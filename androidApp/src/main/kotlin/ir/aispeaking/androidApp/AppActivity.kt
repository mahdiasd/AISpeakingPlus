package ir.aispeaking.androidApp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import ir.aispeaking.chat.stt.AndroidAudioCapture
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.ui.them.AppTheme

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hand the application context to the cross-platform AudioCapture
        // so the Android `actual` (backed by AudioRecord) can check the
        // RECORD_AUDIO permission and open the mic.
        AndroidAudioCapture.attach(applicationContext)
        initKoin()
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation()
            }
        }
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
