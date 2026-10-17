package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
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
 * One message from the AI character. The English text is the hero; translation, audio and copy
 * live in a labelled action row beneath it so each control says what it does.
 */
@Composable
fun AiChatItem(
    modifier: Modifier = Modifier,
    chat: Chat.Ai,
    characterName: String = "",
    characterAvatarUrl: String? = null,
    isLastChat: Boolean = false,
    onPlayVoice: () -> Unit = {},
    onStopVoice: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var showTranslation by remember { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }
    val hasTranslation = !chat.translatedMessage.isNullOrBlank()

    if (copied) {
        LaunchedEffect(Unit) {
            delay(1500)
            copied = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameAvatar(name = characterName, imageUrl = characterAvatarUrl, size = 34.dp)

            Column(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val bubbleShape = RoundedCornerShape(
                    topStart = 6.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp
                )
                Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .background(Game.BubbleAi, bubbleShape)
                        .border(1.dp, Game.Stroke, bubbleShape)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                        .background(Game.Stroke)
                                )
                                GameText(
                                    text = chat.translatedMessage ?: "",
                                    size = 14.sp,
                                    lineHeight = 23.sp,
                                    color = Game.TextSecondary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Labelled actions
                if (chat.message.isNotBlank()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        when (chat.voiceState) {
                            AiVoiceState.PendingToPlay -> GameChip(text = "در حال آماده‌سازی", accent = Game.Mint)
                            AiVoiceState.Playing -> PlayingVoiceChip(onClick = onStopVoice)
                            AiVoiceState.Stopped -> if (!chat.audioUrl.isNullOrBlank()) {
                                GameChip(
                                    text = "پخش صدا",
                                    icon = Res.drawable.ic_play,
                                    accent = Game.Mint,
                                    onClick = onPlayVoice
                                )
                            }
                        }
                        if (hasTranslation) {
                            GameChip(
                                text = if (showTranslation) "پنهان کردن ترجمه" else "ترجمه",
                                icon = Res.drawable.ic_translate,
                                accent = Game.Sky,
                                active = showTranslation,
                                onClick = { showTranslation = !showTranslation }
                            )
                        }
                        GameChip(
                            text = if (copied) "کپی شد" else "کپی",
                            icon = if (copied) Res.drawable.ic_done else Res.drawable.ic_copy,
                            accent = if (copied) Game.Mint else Game.TextPrimary,
                            active = copied,
                            onClick = {
                                clipboardManager.setText(AnnotatedString(chat.message))
                                copied = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayingVoiceChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f),
        label = "playing_voice_press"
    )
    val shape = RoundedCornerShape(50)

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
    val bar4 by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar4"
    )

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(Game.Mint.copy(alpha = 0.28f))
            .border(1.dp, Game.Mint.copy(alpha = 0.75f), shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_speaker_off),
            contentDescription = "توقف صدا",
            tint = Game.Mint,
            modifier = Modifier.size(15.dp)
        )
        Row(
            modifier = Modifier.height(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            val bars = listOf(bar1, bar2, bar3, bar4)
            bars.forEach { barVal ->
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height((4 + 10 * barVal).dp)
                        .background(Game.Mint, RoundedCornerShape(1.dp))
                )
            }
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
