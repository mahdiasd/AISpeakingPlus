package ir.aispeaking.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import ir.aispeaking.sharedui.ic_logo
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.video.VideoPlayerBridgeHolder
import org.jetbrains.compose.resources.painterResource

/**
 * Welcoming onboarding & intro video briefing screen.
 * Displays a warm, conversational explanation of the game's story journey (Tehran to London),
 * explains why watching the cinematic video sets the context for all speaking missions,
 * and allows the user to play the video with a single tap or skip straight to the game.
 */
@Composable
fun IntroVideoScreen(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    videoUrl: String = "/resources/video/intro.mp4"
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
                            text = "در حال پخش داستان سفر...",
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
                // Briefing Screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        GameButton(
                            text = "رد کردن ›",
                            onClick = onSkip,
                            style = GameButtonStyle.Glass,
                            height = 36.dp,
                            textSize = 12.sp
                        )

                        GameChip(
                            text = "داستان سفر: تهران تا لندن",
                            active = true,
                            onClick = {}
                        )
                    }

                    // Scrollable Briefing Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(4.dp))

                        // Glowing Hero Avatar
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .background(Color(0x220A84FF))
                                .border(1.5.dp, Color(0x550A84FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_logo),
                                contentDescription = "AI Speaking Hero",
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                            )
                        }

                        // Welcoming Heading
                        GameText(
                            text = "سلام همسفر! خیلی خوش اومدی 👋",
                            size = 21.sp,
                            bold = true,
                            align = TextAlign.Center
                        )

                        // Warm & Conversational "Why Watch" Explanation
                        GameText(
                            text = "اینجا یادگیری زبان مثل یک بازی واقعیه؛ قراره پا جای پای کسی بذاری که چمدونش رو در تهران بسته و راهی لندن شده!\nاز سفارش غذا توی هواپیما گرفته تا گپ زدن با هم‌اتاقی‌ها و مصاحبه کاری، همه مکالمه‌ها تکه‌ای از همین ماجراجویی هستن.\n\nدیدن این ویدیوی کوتاه ۱۰ ثانیه‌ای کمکت می‌کنه حس و حال شروع سفر رو لمس کنی و بدونی در طول بازی با چه کسی و چرا داری انگلیسی صحبت می‌کنی.",
                            size = 14.sp,
                            lineHeight = 24.sp,
                            color = Game.TextSecondary,
                            align = TextAlign.Center,
                            modifier = Modifier.widthIn(max = 440.dp)
                        )

                        // 3 Key Story Pillars in Liquid Glass Panel
                        GlassPanel(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 440.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StoryPillarRow(
                                    emoji = "✈️",
                                    title = "آغاز پرواز در دل شب",
                                    desc = "شروع واقعی با اولین مکالمه روی صندلی پرواز به لندن"
                                )
                                StoryPillarRow(
                                    emoji = "🎭",
                                    title = "شخصیت‌ها و چالش‌های زنده",
                                    desc = "تمرین انگلیسی در شرایط و موقعیت‌های طبیعی روزمره"
                                )
                                StoryPillarRow(
                                    emoji = "🎯",
                                    title = "ماموریت‌های زبانی هدفمند",
                                    desc = "هر مکالمه یک هدف مشخص داره تا قدم‌به‌قدم پیشرفت کنی"
                                )
                            }
                        }

                        Spacer(Modifier.height(4.dp))

                        // Primary Action: Watch Video
                        GameButton(
                            text = "تماشای داستان شروع سفر (۱۰ ثانیه) ▶",
                            icon = Res.drawable.ic_play,
                            onClick = { isPlayingVideo = true },
                            style = GameButtonStyle.Primary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 440.dp)
                                .height(52.dp),
                            textSize = 15.sp
                        )

                        // Secondary Action: Skip
                        GameButton(
                            text = "رد کردن و ورود مستقیم به مراحل ›",
                            onClick = onSkip,
                            style = GameButtonStyle.Glass,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 440.dp)
                                .height(44.dp),
                            textSize = 13.sp
                        )

                        // Footer Replay Note
                        GameText(
                            text = "می‌تونی این ویدیو رو هر زمان که خواستی از بخش پروفایلت هم دوباره تماشا کنی.",
                            size = 12.sp,
                            color = Game.TextMuted,
                            align = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryPillarRow(
    emoji: String,
    title: String,
    desc: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0x22FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            GameText(text = emoji, size = 18.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            GameText(
                text = title,
                size = 13.sp,
                bold = true,
                color = Game.TextPrimary
            )
            GameText(
                text = desc,
                size = 11.5.sp,
                lineHeight = 16.sp,
                color = Game.TextSecondary
            )
        }
    }
}
