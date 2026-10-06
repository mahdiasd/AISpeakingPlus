package ir.aispeaking.sharedui.ui.stage.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_clear
import ir.aispeaking.sharedui.ic_keyboard
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_send
import ir.aispeaking.sharedui.ic_voice
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.lottie.LottieLoader
import androidx.compose.ui.graphics.Color
import ir.aispeaking.sharedui.ui.core.input.textFieldColors
import ir.aispeaking.sharedui.ui.them.AppTheme

enum class ChatInputMode {
    VOICE,
    TEXT
}

@Composable
fun ChatBottomBar(
    modifier: Modifier = Modifier,
    inputMode: ChatInputMode,
    text: String,
    isRecording: Boolean,
    onTextChange: (String) -> Unit,
    onInputModeChange: (ChatInputMode) -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        when (inputMode) {
            ChatInputMode.VOICE -> {
                VoiceBottomBarContent(
                    text = text,
                    isRecording = isRecording,
                    onSwitchToText = { onInputModeChange(ChatInputMode.TEXT) },
                    onVoiceToggle = onVoiceToggle,
                    onSendClick = onSendClick
                )
            }
            ChatInputMode.TEXT -> {
                TextBottomBarContent(
                    text = text,
                    onTextChange = onTextChange,
                    onSwitchToVoice = { onInputModeChange(ChatInputMode.VOICE) },
                    onSendClick = onSendClick
                )
            }
        }
    }
}

@Composable
private fun VoiceBottomBarContent(
    text: String,
    isRecording: Boolean,
    onSwitchToText: () -> Unit,
    onVoiceToggle: (Boolean) -> Unit,
    onSendClick: () -> Unit
) {
    val canSend = text.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Keyboard switch button
        AppIcon(
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .background(AppTheme.colors.primary, CircleShape)
                .padding(10.dp),
            size = 46.dp,
            tint = AppTheme.colors.onPrimary,
            icon = Res.drawable.ic_keyboard,
            onClick = onSwitchToText
        )

        // Center Microphone / Wave Record Button
        AnimatedContent(
            targetState = isRecording,
            modifier = Modifier.size(80.dp)
        ) { recording ->
            if (recording) {
                LottieLoader(
                    modifier = Modifier
                        .size(75.dp)
                        .shadow(4.dp, CircleShape)
                        .background(AppTheme.colors.primary, CircleShape)
                        .animateClickable { onVoiceToggle(false) },
                    jsonPath = "files/lt_recording_anim.json",
                    tint = AppTheme.colors.onPrimary
                )
            } else {
                AppIcon(
                    modifier = Modifier
                        .shadow(4.dp, CircleShape)
                        .background(AppTheme.colors.primary, CircleShape)
                        .padding(16.dp),
                    size = 75.dp,
                    tint = AppTheme.colors.onPrimary,
                    icon = Res.drawable.ic_microphone,
                    onClick = { onVoiceToggle(true) }
                )
            }
        }

        // Send Button
        AnimatedContent(canSend) { active ->
            if (active) {
                AppIcon(
                    modifier = Modifier
                        .shadow(2.dp, CircleShape)
                        .background(AppTheme.colors.primary, CircleShape)
                        .padding(10.dp),
                    size = 46.dp,
                    tint = AppTheme.colors.onPrimary,
                    icon = Res.drawable.ic_send,
                    onClick = onSendClick
                )
            } else {
                AppIcon(
                    modifier = Modifier
                        .shadow(1.dp, CircleShape)
                        .background(AppTheme.colors.surfaceContainerHighest, CircleShape)
                        .padding(10.dp),
                    size = 46.dp,
                    tint = AppTheme.colors.outlineVariant,
                    icon = Res.drawable.ic_send,
                    onClick = null
                )
            }
        }
    }
}

@Composable
private fun TextBottomBarContent(
    text: String,
    onTextChange: (String) -> Unit,
    onSwitchToVoice: () -> Unit,
    onSendClick: () -> Unit
) {
    val canSend = text.isNotBlank()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Switch to Voice button
        AppIcon(
            modifier = Modifier
                .shadow(2.dp, CircleShape)
                .background(AppTheme.colors.primary, CircleShape)
                .padding(8.dp),
            icon = Res.drawable.ic_voice,
            size = 42.dp,
            tint = AppTheme.colors.onPrimary,
            onClick = onSwitchToVoice
        )

        // Text input field
        AppTextField(
            modifier = Modifier.weight(1f),
            value = text,
            textDirection = TextDirection.Ltr,
            maxLines = 4,
            shape = AppTheme.shapes.roundMedium,
            onValueChange = onTextChange,
            hint = "Type your response in English...",
            textStyle = AppTheme.typography.bodyMedium.copy(color = Color.White),
            placeholderTextStyle = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.5f)),
            colors = textFieldColors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White.copy(alpha = 0.8f),
                unfocusedBorderColor = Color.White.copy(alpha = 0.4f),
                focusedContainerColor = Color.Black.copy(alpha = 0.35f),
                unfocusedContainerColor = Color.Black.copy(alpha = 0.25f)
            ),
            leadingIcon = if (text.isNotEmpty()) {
                {
                    AppIcon(
                        modifier = Modifier
                            .background(AppTheme.colors.primary, shape = CircleShape)
                            .padding(4.dp),
                        icon = Res.drawable.ic_clear,
                        size = 24.dp,
                        tint = AppTheme.colors.onPrimary,
                        onClick = { onTextChange("") }
                    )
                }
            } else null
        )

        // Send Button
        AnimatedContent(canSend) { active ->
            if (active) {
                AppIcon(
                    modifier = Modifier
                        .shadow(2.dp, CircleShape)
                        .background(AppTheme.colors.primary, CircleShape)
                        .padding(10.dp),
                    size = 42.dp,
                    tint = AppTheme.colors.onPrimary,
                    icon = Res.drawable.ic_send,
                    onClick = onSendClick
                )
            } else {
                AppIcon(
                    modifier = Modifier
                        .shadow(1.dp, CircleShape)
                        .background(AppTheme.colors.surfaceContainerHighest, CircleShape)
                        .padding(10.dp),
                    size = 42.dp,
                    tint = AppTheme.colors.outlineVariant,
                    icon = Res.drawable.ic_send,
                    onClick = null
                )
            }
        }
    }
}
