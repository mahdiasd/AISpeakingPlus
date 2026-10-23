package ir.aispeaking.main

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.video.VideoPlayerBridgeHolder

/**
 * Full-screen cinematic intro video screen.
 * Triggers video playback on platform bridge (HTML5 video overlay on Web, native player on mobile),
 * and provides responsive controls including a skip action and direct continuation.
 */
@Composable
fun IntroVideoScreen(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    videoUrl: String = "/resources/video/intro.mp4"
) {
    DisposableEffect(Unit) {
        VideoPlayerBridgeHolder.bridge?.play(
            url = videoUrl,
            onEnded = onFinished,
            onSkip = onSkip
        )
        onDispose {
            VideoPlayerBridgeHolder.bridge?.stop()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "IntroPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF090A10),
                            Color(0xFF141726),
                            Color(0xFF090A10)
                        )
                    )
                )
        ) {
            // Top HUD Bar with Skip Action and Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GameButton(
                    text = "رد کردن ›",
                    onClick = onSkip,
                    style = GameButtonStyle.Glass,
                    height = 38.dp,
                    textSize = 13.sp
                )

                GameText(
                    text = "داستان بازی: تهران تا لندن",
                    size = 13.sp,
                    color = Game.TextSecondary
                )
            }

            // Center Ambient Graphic & Direct Action
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .border(1.5.dp, Color(0x44FFFFFF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_logo),
                        contentDescription = "AI Speaking Hero",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                    )
                }

                GameText(
                    text = "آغاز ماجراجویی قهرمان داستان",
                    size = 20.sp,
                    bold = true,
                    align = TextAlign.Center
                )

                GameText(
                    text = "سفر بزرگ از تهران به لندن شروع شد. آماده‌ای همراهش باشی؟",
                    size = 14.sp,
                    lineHeight = 22.sp,
                    color = Game.TextSecondary.copy(alpha = pulseAlpha),
                    align = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(8.dp))

                GameButton(
                    text = "ورود مستقیم به نقشه مراحل",
                    icon = Res.drawable.ic_play,
                    onClick = onSkip,
                    style = GameButtonStyle.Primary,
                    modifier = Modifier.width(240.dp)
                )
            }

            // Bottom hint
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                GameText(
                    text = "می‌توانی این ویدیو را بعداً در صفحه پروفایل دوباره تماشا کنی.",
                    size = 12.sp,
                    color = Game.TextMuted,
                    align = TextAlign.Center
                )
            }
        }
    }
}
