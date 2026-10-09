import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.ComposeViewport
import ir.aispeaking.chat.audio.DefaultStageAudioController
import ir.aispeaking.chat.audio.StageAudioController
import ir.aispeaking.navigation.AppNavigation
import ir.aispeaking.navigation.di.initKoin
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.web.WebBrowserNavigation
import ir.aispeaking.web.setupWebAudioBridge
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.GlobalContext

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initKoin()
    try {
        val audioController = (GlobalContext.get().getOrNull<StageAudioController>() as? DefaultStageAudioController)
            ?: GlobalContext.get().getOrNull<DefaultStageAudioController>()
        if (audioController != null) {
            setupWebAudioBridge(audioController)
        }
    } catch (_: Throwable) {}

    ComposeViewport {
        AppTheme(true) {
            // Apple-style dark ambient backdrop shown around the phone frame on large screens.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            0.0f to Color(0xFF161822),
                            0.6f to Color(0xFF0A0C10),
                            1.0f to Color(0xFF000000)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val isWideScreen = maxWidth > 560.dp
                    val showSidePanel = maxWidth > 980.dp

                    if (isWideScreen) {
                        // Desktop / tablet: centered phone-sized stage, empty space around it.
                        val phoneWidth = min(430.dp, maxWidth - 32.dp)
                        val phoneHeight = min(900.dp, maxHeight - 32.dp)
                        val frame = RoundedCornerShape(42.dp)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(72.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(phoneWidth)
                                    .height(phoneHeight)
                                    .shadow(
                                        elevation = 32.dp,
                                        shape = frame,
                                        spotColor = Color(0x77000000),
                                        ambientColor = Color(0x44000000)
                                    )
                                    .border(1.5.dp, Color(0x28FFFFFF), frame)
                                    .clip(frame)
                                    .background(Game.Ink)
                            ) {
                                AppNavigation(onBackStackCreated = { WebBrowserNavigation(it) })
                            }

                            if (showSidePanel) {
                                BrandPanel()
                            }
                        }
                    } else {
                        // Phone browser: full-bleed app, no frame.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Game.Ink)
                        ) {
                            AppNavigation(onBackStackCreated = { WebBrowserNavigation(it) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandPanel() {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            GameText(text = "AI Speaking Plus", size = 30.sp, bold = true, latin = true)
            GameText(
                text = "مکالمه انگلیسی را در یک ماجراجویی واقعی تمرین کن؛ با هوش مصنوعی صحبت کن و ستاره جمع کن.",
                size = 15.sp,
                lineHeight = 25.sp,
                color = Game.TextSecondary
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(Game.Mint, CircleShape)
                )
                GameText(
                    text = "برای بهترین تجربه، روی موبایل باز کن",
                    size = 13.sp,
                    color = Game.Mint
                )
            }
        }
    }
}
