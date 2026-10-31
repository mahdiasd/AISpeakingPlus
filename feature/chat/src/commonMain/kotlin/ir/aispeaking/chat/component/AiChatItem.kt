package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_copy
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_speaker_off
import ir.aispeaking.sharedui.ic_translate
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass AI Message Item.
 *
 * Characteristics:
 * - Floating Liquid Glass bubble with specular light rim on top.
 * - Hero English copy with Apple clarity and high contrast.
 * - Expandable Persian translation with silky smooth transition.
 * - Audio playback chip with dynamic equalizer waveform in Serene Cyan (strictly NO green).
 * - Siri iridescent glowing typing dots when message is generating.
 * - Silky smooth copy feedback with Serene Cyan checkmark.
 */
@Suppress("DEPRECATION")
@Composable
fun AiChatItem(
    modifier: Modifier = Modifier,
    chat: Chat.Ai,
    characterName: String = "",
    characterAvatarUrl: String? = null,
    isLastChat: Boolean = false,
    onPlayVoice: () -> Unit = {},
    onStopVoice: () -> Unit = {},
    onCopied: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var showTranslation by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val hasTranslation = !chat.translatedMessage.isNullOrBlank()

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000)
            copied = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameAvatar(
                name = characterName,
                imageUrl = characterAvatarUrl,
                size = 36.dp,
                ring = AppleChatTheme.SpecularBorderColor
            )

            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val bubbleShape = RoundedCornerShape(
                    topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp
                )

                // Liquid Glass Bubble with Specular Top Highlight
                Column(
                    modifier = Modifier
                        .widthIn(max = 330.dp)
                        .clip(bubbleShape)
                        .background(AppleChatTheme.BubbleAi)
                        .border(1.dp, AppleChatTheme.SpecularStrokeBrush, bubbleShape)
                        .padding(horizontal = 15.dp, vertical = 11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (chat.message.isEmpty()) {
                        IridescentTypingDots()
                    } else {
                        GameText(
                            text = chat.message,
                            size = 16.sp,
                            lineHeight = 24.sp,
                            latin = true,
                            color = Game.TextPrimary
                        )

                        AnimatedVisibility(visible = showTranslation && hasTranslation) {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(AppleChatTheme.SubtleStrokeBrush)
                                    )
                                    GameText(
                                        text = chat.translatedMessage ?: "",
                                        size = 14.sp,
                                        lineHeight = 23.sp,
                                        color = Game.TextSecondary,
                                        align = TextAlign.Justify,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Apple Labelled Action Chips (Stable Size & Zero Layout Shift)
                if (chat.message.isNotBlank()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AiVoiceChip(
                            voiceState = chat.voiceState,
                            hasAudio = !chat.audioUrl.isNullOrBlank(),
                            onPlayVoice = onPlayVoice,
                            onStopVoice = onStopVoice
                        )

                        AiTranslateChip(
                            hasTranslation = hasTranslation,
                            showTranslation = showTranslation,
                            onToggleTranslation = { showTranslation = !showTranslation }
                        )

                        GameChip(
                            text = if (copied) "کپی شد" else "کپی",
                            icon = if (copied) Res.drawable.ic_done else Res.drawable.ic_copy,
                            accent = AppleChatTheme.PrimaryAzure,
                            border = AppleChatTheme.SubtleBorderColor,
                            active = copied,
                            keepContainerOnActive = true,
                            modifier = Modifier.defaultMinSize(minWidth = 72.dp, minHeight = 32.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(chat.message))
                                copied = true
                                onCopied()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Unified Audio Voice Chip with 100% stable width across Pending, Stopped, and Playing states.
 */
@Composable
private fun AiVoiceChip(
    voiceState: AiVoiceState,
    hasAudio: Boolean,
    onPlayVoice: () -> Unit,
    onStopVoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPlaying = voiceState == AiVoiceState.Playing
    val isPending = voiceState == AiVoiceState.PendingToPlay || !hasAudio
    val isReady = hasAudio && voiceState == AiVoiceState.Stopped

    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(50)
    val accent = AppleChatTheme.PrimaryAzure
    val containerBg = if (isDark) Color(0xEB181A22) else Color(0xCCFFFFFF)

    val targetAlpha = if (isPending) 0.42f else 1f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(220),
        label = "ai_voice_chip_alpha"
    )

    val strokeColor = when {
        isPlaying -> accent.copy(alpha = 0.65f)
        isReady -> AppleChatTheme.SubtleBorderColor
        else -> AppleChatTheme.SubtleBorderColor.copy(alpha = 0.4f)
    }

    val transition = rememberInfiniteTransition(label = "audio_wave_pulse")
    val bar1 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    val clickAction: (() -> Unit)? = when {
        isPlaying -> onStopVoice
        isReady -> onPlayVoice
        else -> null
    }

    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = alphaAnim }
            .applePressEffect(enabled = !isPending, targetScale = 0.95f, onClick = clickAction)
            .clip(shape)
            .background(containerBg)
            .border(if (isPlaying) 1.5.dp else 1.dp, strokeColor, shape)
            .defaultMinSize(minWidth = 104.dp, minHeight = 32.dp)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isPlaying) {
            Icon(
                painter = painterResource(Res.drawable.ic_speaker_off),
                contentDescription = "توقف صدا",
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            GameText(
                text = "پخش صدا",
                size = 13.sp,
                color = accent,
                bold = true
            )
            Spacer(Modifier.width(5.dp))
            Row(
                modifier = Modifier.height(13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.5.dp)
            ) {
                listOf(bar1, bar2, bar3).forEach { barVal ->
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height((3 + 9 * barVal).dp)
                            .background(accent, RoundedCornerShape(1.dp))
                    )
                }
            }
        } else {
            Icon(
                painter = painterResource(Res.drawable.ic_play),
                contentDescription = "پخش صدا",
                tint = if (isReady) accent else Game.TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(5.dp))
            GameText(
                text = "پخش صدا",
                size = 13.sp,
                color = if (isReady) Game.TextPrimary else Game.TextSecondary.copy(alpha = 0.7f),
                bold = isReady
            )
        }
    }
}

/**
 * Unified Translation Action Chip with 100% stable width across Pending, Closed, and Open states.
 */
@Composable
private fun AiTranslateChip(
    hasTranslation: Boolean,
    showTranslation: Boolean,
    onToggleTranslation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = !hasTranslation
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(50)
    val accent = AppleChatTheme.PrimaryAzure
    val containerBg = if (isDark) Color(0xEB181A22) else Color(0xCCFFFFFF)

    val targetAlpha = if (isPending) 0.42f else 1f
    val alphaAnim by animateFloatAsState(
        targetValue = targetAlpha,
        animationSpec = tween(220),
        label = "ai_translate_chip_alpha"
    )

    val isActive = showTranslation && hasTranslation
    val strokeColor = when {
        isActive -> accent.copy(alpha = 0.65f)
        hasTranslation -> AppleChatTheme.SubtleBorderColor
        else -> AppleChatTheme.SubtleBorderColor.copy(alpha = 0.4f)
    }

    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = alphaAnim }
            .applePressEffect(enabled = hasTranslation, targetScale = 0.95f, onClick = onToggleTranslation)
            .clip(shape)
            .background(containerBg)
            .border(if (isActive) 1.5.dp else 1.dp, strokeColor, shape)
            .defaultMinSize(minWidth = 84.dp, minHeight = 32.dp)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_translate),
            contentDescription = "ترجمه",
            tint = if (isActive || hasTranslation) accent else Game.TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(5.dp))
        GameText(
            text = "ترجمه",
            size = 13.sp,
            color = if (isActive) accent else if (hasTranslation) Game.TextPrimary else Game.TextSecondary.copy(alpha = 0.7f),
            bold = isActive
        )
    }
}

/**
 * Siri / Apple Intelligence Iridescent Typing Dots.
 */
@Composable
private fun IridescentTypingDots(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "ai_typing_dots")
    val dotColors = listOf(
        AppleChatTheme.SiriViolet,
        AppleChatTheme.SiriCyan,
        AppleChatTheme.SiriAzure
    )

    Row(
        modifier = modifier.padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0.3f at index * 160 using LinearEasing
                        1f at index * 160 + 260 using LinearEasing
                        0.3f at index * 160 + 520 using LinearEasing
                    },
                    repeatMode = RepeatMode.Restart
                ),
                label = "ai_dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(dotColors[index % dotColors.size], CircleShape)
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AiChatItemPreview() {
    AppTheme {
        AiChatItem(
            chat = Chat.Ai(
                uid = "1",
                message = "Hello! How can I help you today?",
                translatedMessage = "سلام! امروز چطور می‌توانم به شما کمک کنم؟"
            ),
            characterName = "Emily"
        )
    }
}
