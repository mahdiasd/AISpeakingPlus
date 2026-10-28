package ir.aispeaking.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.video.VideoPlayerBridgeHolder
import org.jetbrains.compose.resources.painterResource

/**
 * Clean & minimalistic onboarding intro video briefing screen.
 * Keeps the interface uncluttered, presenting only essential actions:
 * play the intro video or proceed straight to the main game.
 */
@Composable
fun IntroVideoScreen(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    videoUrl: String = "/resources/video/intro.mp4?v=2"
) {
    var isPlayingVideo by remember { mutableStateOf(false) }

    if (isPlayingVideo) {
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
    }

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
            if (isPlayingVideo) {
                // Ambient backdrop while video overlay is active
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameText(
                            text = "در حال پخش داستان بازی...",
                            size = 15.sp,
                            color = Game.TextSecondary
                        )
                        GameButton(
                            text = "رد کردن و ورود به مراحل ›",
                            onClick = onSkip,
                            style = GameButtonStyle.Glass,
                            height = 38.dp,
                            textSize = 13.sp
                        )
                    }
                }
            } else {
                // Clean Minimalist Briefing Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // Top Bar (RTL: Start = Right, End = Left)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Start Anchor: Back Button
                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppBackButton(onClick = onSkip)
                        }

                        // Symmetrical Center Title
                        GameText(
                            text = "داستان بازی",
                            size = 17.sp,
                            bold = true,
                            align = TextAlign.Center
                        )

                        // End Anchor: Quick Skip Button
                        Row(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GameButton(
                                text = "رد کردن",
                                onClick = onSkip,
                                style = GameButtonStyle.Glass,
                                height = 36.dp,
                                textSize = 12.sp
                            )
                        }
                    }

                    // Centered Content
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 380.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Spacer(Modifier.height(10.dp))

                            // Glowing Hero Play Preview Badge
                            Box(
                                modifier = Modifier
                                    .size(108.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0x330A84FF),
                                                Color(0x120A84FF),
                                                Color(0x050A84FF)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                Color(0x880A84FF),
                                                Color(0x220A84FF)
                                            )
                                        ),
                                        shape = CircleShape
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { isPlayingVideo = true }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF007AFF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.ic_play),
                                        contentDescription = "پخش ویدیو",
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }

                            // Title & Clear, Short Story Motivation
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GameText(
                                    text = "داستان بازی چطور شروع می‌شود؟",
                                    size = 20.sp,
                                    bold = true,
                                    align = TextAlign.Center
                                )

                                GameText(
                                    text = "تمام تمرین‌ها و مراحل این برنامه، مأموریت‌های یک سفر واقعی از تهران تا لندن هستند. این ویدیوی کوتاه را ببینید تا با هدف بازی و نقش خودتان در این مسیر آشنا شوید.",
                                    size = 14.sp,
                                    lineHeight = 23.sp,
                                    color = Game.TextSecondary,
                                    align = TextAlign.Justify
                                )
                            }

                            Spacer(Modifier.height(8.dp))

                            // Action Buttons
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                GameButton(
                                    text = "تماشای داستان بازی",
                                    icon = Res.drawable.ic_play,
                                    onClick = { isPlayingVideo = true },
                                    style = GameButtonStyle.Primary,
                                    height = 50.dp,
                                    textSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                GameButton(
                                    text = "ورود مستقیم به مراحل",
                                    onClick = onSkip,
                                    style = GameButtonStyle.Glass,
                                    height = 46.dp,
                                    textSize = 14.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}
