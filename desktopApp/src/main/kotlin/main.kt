import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.ui.them.AppTheme

fun main() {
    initKoin()
    application {
        Window(
            title = "AiSpeakingMultiplatform",
            state = rememberWindowState(width = 430.dp, height = 880.dp),
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(350, 600)
            AppTheme(true) {
                AppNavigation()
            }
        }
    }
}

