package ir.aispeaking.chat.component.chat_item

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobilebytelabs.kmptoolkit.clipboard.copyToClipboard
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.DialogType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_copy
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_translate
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.SelectableText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.toCorner
import ir.aispeaking.sharedui.ui.lottie.LottieLoader
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun AiChatItem(
    modifier: Modifier,
    chat: Chat.Ai,
    isLastChat: Boolean = false,
    onAction: OnAction
) {
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
        ) {
            if (it) {
                BodyMediumText(text = chat.translatedMessage ?: "", persianFont = true)
            } else {
                SelectableText(
                    modifier = Modifier.fillMaxWidth(),
                    text = chat.message,
                    enable = !(showTranslatedMessage && !chat.translatedMessage.isNullOrEmpty()),
                    onSelected = { selectedText ->
                        onAction(ChatUiEvent.OnDialogType(DialogType.SelectText(selectedText)))
                    },
                )
            }
        }

        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {

            AppIcon(
                modifier = Modifier
                    .animateClickable { showTranslatedMessage = !showTranslatedMessage }
                    .background(color = AppTheme.colors.aiChatContainer, CircleShape)
                    .padding(6.dp),
                size = 32.dp,
                tint = if (showTranslatedMessage) AppTheme.colors.primary else AppTheme.colors.onSurface,
                icon = Res.drawable.ic_translate
            )

            AppIcon(
                modifier = Modifier
                    .animateClickable { copyToClipboard(chat.message) }
                    .background(color = AppTheme.colors.aiChatContainer, CircleShape)
                    .padding(6.dp),
                size = 32.dp,
                tint = AppTheme.colors.onSurface,
                icon = Res.drawable.ic_copy
            )

            if (chat.fetchingSuggest) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .animateClickable { onAction(ChatUiEvent.GetSuggests) }
                        .size(32.dp)
                        .background(color = AppTheme.colors.aiChatContainer, CircleShape)
                        .padding(6.dp),
                    strokeWidth = 0.8.dp,
                    color = AppTheme.colors.onPrimary
                )
            } else if (chat.suggests.isNullOrEmpty() && isLastChat) {
                AppIcon(
                    modifier = Modifier
                        .animateClickable { onAction(ChatUiEvent.GetSuggests) }
                        .background(color = AppTheme.colors.aiChatContainer, CircleShape)
                        .padding(6.dp),
                    size = 32.dp,
                    tint = AppTheme.colors.onSurface,
                    icon = Res.drawable.ic_lamp
                )
            }

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
                            Modifier
                                .size(32.dp)
                                .animateClickable { onAction(ChatUiEvent.OnChangeAiVoiceState(chat.uid, AiVoiceState.Stopped)) },
                            jsonPath = "files/lt_recording_anim.json",
                            tint = AppTheme.colors.onSurface
                        )
                    }

                    AiVoiceState.Stopped -> AppIcon(
                        modifier = Modifier
                            .animateClickable { onAction(ChatUiEvent.OnChangeAiVoiceState(chat.uid, AiVoiceState.PendingToPlay)) }
                            .background(color = AppTheme.colors.aiChatContainer, shape = CircleShape)
                            .padding(8.dp),
                        size = 32.dp,
                        icon = Res.drawable.ic_play
                    )
                }
            }
        }

    }
}


@LightDarkPreview
@Composable
private fun SmallTextPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth())
        {
            AiChatItem(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(alignment = Alignment.CenterStart),
                chat = FakeData.provideAiChats().first().copy(message = "Hello"),
                onAction = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun BigTextPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth())
        {
            AiChatItem(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .align(alignment = Alignment.CenterStart),
                chat = FakeData.provideAiChats().first(),
                isLastChat = true,
                onAction = {}
            )
        }
    }
}
