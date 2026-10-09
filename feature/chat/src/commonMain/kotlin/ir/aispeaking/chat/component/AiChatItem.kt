package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_copy
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_translate
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.SelectableText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.toCorner
import ir.aispeaking.sharedui.ui.lottie.LottieLoader
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun AiChatItem(
    modifier: Modifier = Modifier,
    chat: Chat.Ai,
    isLastChat: Boolean = false,
    onPlayVoice: () -> Unit = {},
    onStopVoice: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current
    var showTranslatedMessage by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        AnimatedContent(
            modifier = Modifier
                .weight(1f, fill = false)
                .background(
                    color = AppTheme.colors.aiChatContainer,
                    shape = AppTheme.shapes.roundMedium.copy(bottomStart = 0.dp.toCorner())
                )
                .padding(8.dp),
            targetState = showTranslatedMessage && !chat.translatedMessage.isNullOrEmpty()
        ) { isTranslated ->
            if (isTranslated) {
                BodyMediumText(text = chat.translatedMessage ?: "", persianFont = true)
            } else {
                SelectableText(
                    modifier = Modifier.fillMaxWidth(),
                    text = chat.message,
                    enable = !(showTranslatedMessage && !chat.translatedMessage.isNullOrEmpty()),
                    onSelected = { }
                )
            }
        }

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            // Translate toggle
            AppIcon(
                modifier = Modifier
                    .animateClickable { showTranslatedMessage = !showTranslatedMessage }
                    .background(color = AppTheme.colors.aiChatContainer, shape = CircleShape)
                    .padding(6.dp),
                size = 32.dp,
                tint = if (showTranslatedMessage) AppTheme.colors.primary else AppTheme.colors.onSurface,
                icon = Res.drawable.ic_translate
            )

            // Copy to clipboard
            AppIcon(
                modifier = Modifier
                    .animateClickable { clipboardManager.setText(AnnotatedString(chat.message)) }
                    .background(color = AppTheme.colors.aiChatContainer, shape = CircleShape)
                    .padding(6.dp),
                size = 32.dp,
                tint = AppTheme.colors.onSurface,
                icon = Res.drawable.ic_copy
            )

            // Voice Play / Stop / Loading
            AnimatedContent(chat.voiceState) { voiceState ->
                when (voiceState) {
                    AiVoiceState.PendingToPlay -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            strokeWidth = 2.dp,
                            color = AppTheme.colors.primary
                        )
                    }

                    AiVoiceState.Playing -> {
                        LottieLoader(
                            modifier = Modifier
                                .size(32.dp)
                                .animateClickable { onStopVoice() },
                            jsonPath = "files/lt_recording_anim.json",
                            tint = AppTheme.colors.onSurface
                        )
                    }

                    AiVoiceState.Stopped -> {
                        AppIcon(
                            modifier = Modifier
                                .animateClickable { onPlayVoice() }
                                .background(color = AppTheme.colors.aiChatContainer, shape = CircleShape)
                                .padding(8.dp),
                            size = 32.dp,
                            icon = Res.drawable.ic_play,
                            tint = AppTheme.colors.primary
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
            )
        )
    }
}
