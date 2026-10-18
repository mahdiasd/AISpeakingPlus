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
    private var pendingRecordAudioCallback: ((Boolean) -> Unit)? = null

    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val cb = pendingRecordAudioCallback
        pendingRecordAudioCallback = null
        cb?.invoke(isGranted)
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
                ir.aispeaking.androidApp.payment.androidPaymentModule(applicationContext)
            )
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

        // The app is a dark, full-bleed game UI: keep system bar icons light on transparent bars.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            AppTheme(darkTheme = true) {
                AppNavigation()
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
    }

    override fun onDestroy() {
        super.onDestroy()
        if (ir.aispeaking.androidApp.payment.CurrentActivityHolder.currentActivity === this) {
            ir.aispeaking.androidApp.payment.CurrentActivityHolder.currentActivity = null
        }
        audioBridge?.stopRecordingAudio()
        audioBridge?.stopAudio()
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
            isAppearanceLightStatusBars = isDark
            isAppearanceLightNavigationBars = isDark
        }
    }
}
