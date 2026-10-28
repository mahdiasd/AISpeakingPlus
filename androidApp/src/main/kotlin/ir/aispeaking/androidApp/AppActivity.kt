package ir.aispeaking.androidApp

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.ui.them.AppTheme

import ir.aispeaking.chat.audio.DefaultStageAudioController
import ir.aispeaking.chat.audio.StageAudioController
import org.koin.core.context.GlobalContext

class AppActivity : ComponentActivity() {
    private var audioBridge: AndroidAudioBridge? = null
    private var videoBridge: AndroidVideoBridge? = null
    private var pendingRecordAudioCallback: ((Boolean) -> Unit)? = null

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val cb = pendingRecordAudioCallback
        pendingRecordAudioCallback = null
        cb?.invoke(isGranted)
    }

    private val postNotificationsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        co.touchlab.kermit.Logger.withTag("AppActivity").i { "POST_NOTIFICATIONS permission granted: $isGranted" }
    }

    private fun checkAndRequestPostNotificationsPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val currentPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (currentPermission != PackageManager.PERMISSION_GRANTED) {
                try {
                    postNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } catch (e: Exception) {
                    co.touchlab.kermit.Logger.withTag("AppActivity").w(e) { "Failed to launch POST_NOTIFICATIONS permission request" }
                }
            }
        }
    }

    private fun checkAndRequestRecordAudioPermission(onResult: (Boolean) -> Unit) {
        val currentPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        )
        if (currentPermission == PackageManager.PERMISSION_GRANTED) {
            onResult(true)
        } else {
            pendingRecordAudioCallback = onResult
            try {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } catch (_: Exception) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    RECORD_AUDIO_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            val cb = pendingRecordAudioCallback
            pendingRecordAudioCallback = null
            cb?.invoke(granted)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ir.aispeaking.androidApp.payment.CurrentActivityHolder.currentActivity = this
        initKoin()
        try {
            org.koin.core.context.loadKoinModules(
                listOf(
                    ir.aispeaking.androidApp.payment.androidPaymentModule(applicationContext),
                    ir.aispeaking.androidApp.analytics.androidFirebaseModule(applicationContext)
                )
            )
        } catch (_: Throwable) {}

        try {
            val analytics = GlobalContext.get().getOrNull<ir.aispeaking.utils.analytics.AnalyticsTracker>()
            analytics?.logEvent(
                ir.aispeaking.utils.analytics.AnalyticsEvents.EVENT_APP_OPEN,
                mapOf(
                    ir.aispeaking.utils.analytics.AnalyticsEvents.PARAM_PLATFORM to "android",
                    "device_model" to android.os.Build.MODEL,
                    "os_version" to android.os.Build.VERSION.SDK_INT
                )
            )
            analytics?.setUserProperty(ir.aispeaking.utils.analytics.AnalyticsEvents.USER_PROP_PLATFORM, "android")

            val crashReporter = GlobalContext.get().getOrNull<ir.aispeaking.utils.crashlytics.CrashReporter>()
            crashReporter?.setCustomKey("platform", "android")
            crashReporter?.setCustomKey("os_version", android.os.Build.VERSION.SDK_INT)
            crashReporter?.setCustomKey("device_model", android.os.Build.MODEL)
        } catch (_: Throwable) {}

        try {
            val audioController = GlobalContext.get().getOrNull<DefaultStageAudioController>()
                ?: (GlobalContext.get().getOrNull<StageAudioController>() as? DefaultStageAudioController)
            if (audioController != null) {
                audioBridge = AndroidAudioBridge(
                    context = this,
                    controller = audioController,
                    onRequestRecordPermission = { callback ->
                        checkAndRequestRecordAudioPermission(callback)
                    }
                ).apply {
                    attach()
                }
            }
        } catch (_: Throwable) {}

        try {
            videoBridge = AndroidVideoBridge(this).apply {
                attach()
            }
        } catch (_: Throwable) {}

        // Initialize Notification channel and request Android 13+ permission
        ir.aispeaking.androidApp.notification.NotificationHelper.createNotificationChannel(this)
        checkAndRequestPostNotificationsPermission()

        // Fetch and register FCM Token
        try {
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        co.touchlab.kermit.Logger.withTag("AppActivity").i { "FCM Token: $token" }
                        val analytics = GlobalContext.get().getOrNull<ir.aispeaking.utils.analytics.AnalyticsTracker>()
                        analytics?.setUserProperty("fcm_token", token)
                    }
                }
            // Subscribe to dedicated AISpeaking Plus topic (isolated from legacy app)
            com.google.firebase.messaging.FirebaseMessaging.getInstance()
                .subscribeToTopic("aispeaking_plus_v1")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        co.touchlab.kermit.Logger.withTag("AppActivity").i { "Subscribed to topic: aispeaking_plus_v1" }
                    }
                }
        } catch (_: Throwable) {}

        // Handle cold-start deep link or notification intent
        handleIntentForDeepLink(intent)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        setContent {
            AppTheme {
                ThemeChanged(AppTheme.isDark)
                AppNavigation(onExitApp = { finishAffinity() })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ir.aispeaking.androidApp.payment.CurrentActivityHolder.currentActivity = this
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntentForDeepLink(intent)
    }

    private fun handleIntentForDeepLink(intent: android.content.Intent?) {
        if (intent == null) return
        val uri = intent.dataString
        if (!uri.isNullOrBlank()) {
            ir.aispeaking.navigation.AppDeepLinkManager.handleUrl(uri)
            logDeepLinkAnalytics(url = uri)
            return
        }

        val extras = intent.extras
        if (extras != null && !extras.isEmpty) {
            val dataMap = mutableMapOf<String, String>()
            for (key in extras.keySet()) {
                val value = extras.get(key)?.toString()
                if (value != null) {
                    dataMap[key] = value
                }
            }
            val handled = ir.aispeaking.navigation.AppDeepLinkManager.handleDeepLink(dataMap)
            if (handled) {
                logDeepLinkAnalytics(
                    stageId = dataMap["stage_id"] ?: dataMap["stageId"],
                    screen = dataMap["screen"],
                    url = dataMap["url"]
                )
            }
        }
    }

    private fun logDeepLinkAnalytics(stageId: String? = null, screen: String? = null, url: String? = null) {
        try {
            val analytics = GlobalContext.get().getOrNull<ir.aispeaking.utils.analytics.AnalyticsTracker>()
            val params = mutableMapOf<String, Any>("platform" to "android")
            stageId?.let { params["stage_id"] = it }
            screen?.let { params["screen"] = it }
            url?.let { params["url"] = it }
            analytics?.logEvent("notification_opened", params)
        } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ir.aispeaking.androidApp.payment.CurrentActivityHolder.currentActivity === this) {
            ir.aispeaking.androidApp.payment.CurrentActivityHolder.currentActivity = null
        }
        audioBridge?.stopRecordingAudio()
        audioBridge?.stopAudio()
        videoBridge?.detach()
        videoBridge = null
    }

    companion object {
        private const val RECORD_AUDIO_REQUEST_CODE = 2001
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }
}
