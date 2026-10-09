package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                            AiVoiceState.Playing -> GameChip(
                                text = "توقف صدا",
                                icon = Res.drawable.ic_speaker_off,
                                accent = Game.Mint,
                                active = true,
                                onClick = onStopVoice
                            )
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
