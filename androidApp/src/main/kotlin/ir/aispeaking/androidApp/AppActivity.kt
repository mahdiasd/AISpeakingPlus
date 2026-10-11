package ir.aispeaking.androidApp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.ui.them.AppTheme

import ir.aispeaking.chat.audio.DefaultStageAudioController
import ir.aispeaking.chat.audio.StageAudioController
import org.koin.core.context.GlobalContext

class AppActivity : ComponentActivity() {
    private var audioBridge: AndroidAudioBridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initKoin()
        try {
            val audioController = GlobalContext.get().getOrNull<DefaultStageAudioController>()
                ?: (GlobalContext.get().getOrNull<StageAudioController>() as? DefaultStageAudioController)
            if (audioController != null) {
                audioBridge = AndroidAudioBridge(this, audioController).apply {
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

    override fun onDestroy() {
        super.onDestroy()
        audioBridge?.stopAudio()
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
