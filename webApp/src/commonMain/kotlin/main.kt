import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.window.ComposeViewport
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.ui.them.AppTheme

import ir.aispeaking.sharedui.ui.stage.audio.DefaultStageAudioController
import ir.aispeaking.web.WebBrowserNavigation
import ir.aispeaking.web.setupWebAudioBridge
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    try {
        val audioController = GlobalContext.get().getOrNull<DefaultStageAudioController>()
        if (audioController != null) {
            setupWebAudioBridge(audioController)
        }
    } catch (_: Throwable) {}

    ComposeViewport {
        AppTheme(false) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            0.0f to Color(0xFF283445), // Spotlight center
                            0.5f to Color(0xFF161E28), // Dark sleek theme
                            1.0f to Color(0xFF0D1117)  // Deep edges
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val isWideScreen = maxWidth > 500.dp

                    if (isWideScreen) {
                        // Desktop / Laptop mode: Centered mobile device frame
                        val phoneWidth = min(420.dp, maxWidth - 32.dp)
                        val phoneHeight = min(880.dp, maxHeight - 32.dp)

                        Box(
                            modifier = Modifier
                                .width(phoneWidth)
                                .height(phoneHeight)
                                .shadow(
                                    elevation = 32.dp,
                                    shape = RoundedCornerShape(36.dp),
                                    spotColor = Color(0x66000000),
                                    ambientColor = Color(0x33000000)
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(36.dp)
                                )
                                .clip(RoundedCornerShape(36.dp))
                                .background(AppTheme.colors.surface)
                        ) {
                            AppNavigation(onBackStackCreated = { WebBrowserNavigation(it) })
                        }
                    } else {
                        // Mobile browser mode: Full-screen seamless mobile experience
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AppTheme.colors.surface)
                        ) {
                            AppNavigation(onBackStackCreated = { WebBrowserNavigation(it) })
                        }
                    }
                }
            }
        }
    }
}
