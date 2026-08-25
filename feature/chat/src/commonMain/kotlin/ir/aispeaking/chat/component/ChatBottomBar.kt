package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.InputType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.chat.input.Message
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_clear
import ir.aispeaking.sharedui.ic_keyboard
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ic_send
import ir.aispeaking.sharedui.ic_voice
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import ir.aispeaking.sharedui.ui.validation.errorMessage
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.graphicsLayer
import ir.aispeaking.sharedui.ui.lottie.LottieLoader
import ir.aispeaking.utils.dLog
import org.jetbrains.compose.resources.stringResource


@Composable
fun ChatBottomBarContent(
    modifier: Modifier,
    message: Message,
    screenInputType: InputType,
    onAction: OnAction,
    isRecording: Boolean,
) {

    when (screenInputType) {
        InputType.Text -> {
            TextBottomBarContent(
                modifier = modifier,
                message = message,
                onAction = onAction
            )
        }

        InputType.Voice -> {
            VoiceBottomBarContent(
                modifier = modifier,
                message = message,
                onAction = onAction,
                isRecording = isRecording
            )
        }
    }
}

@Composable
private fun TextBottomBarContent(
    modifier: Modifier,
    message: Message,
    onAction: OnAction
) {
    val isDisableSendIcon by remember(message) { mutableStateOf(message.value.isEmpty()) }

    Row(
        modifier = modifier
            .imePadding(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterHorizontally)
    ) {
        AppTextField(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            value = message.value,
            textDirection = TextDirection.Ltr,
            maxLines = 6,
            shape = AppTheme.shapes.roundMedium,
            onValueChange = { onAction(ChatUiEvent.OnMessageChange(it, InputType.Text)) },
            hint = stringResource(message.hint),
            errorText = message.validate().errorMessage()?.let { stringResource(it) },
            isError = (message.shouldValidate && message.validate() is ValidationStatus.Invalid),
            leadingIcon = if (message.value.isNotEmpty()) {
                {
                    AppIcon(
                        modifier = Modifier
                            .shadow(elevation = 1.dp, shape = CircleShape)
                            .background(color = AppTheme.colors.primary, shape = CircleShape)
                            .padding(6.dp),
                        icon = Res.drawable.ic_clear,
                        size = 32.dp,
                        tint = AppTheme.colors.onPrimary,
                        onClick = { onAction(ChatUiEvent.OnMessageChange(message = "", inputType = InputType.Text)) },
                    )
                }
            } else null
        )

        AppIcon(
            modifier = Modifier
                .shadow(elevation = 1.dp, shape = CircleShape)
                .background(color = AppTheme.colors.primary, shape = CircleShape)
                .padding(8.dp),
            icon = Res.drawable.ic_voice,
            size = 42.dp,
            tint = AppTheme.colors.onPrimary,
            onClick = { onAction(ChatUiEvent.OnChangeScreenInputType(inputType = InputType.Voice)) },
        )

        AnimatedContent(isDisableSendIcon) {
            when (it) {
                true -> {
                    AppIcon(
                        modifier = Modifier
                            .shadow(1.dp, CircleShape)
                            .background(
                                color = AppTheme.colors.surfaceContainerHighest,
                                shape = CircleShape
                            )
                            .padding(12.dp),
                        size = 42.dp,
                        tint = AppTheme.colors.outlineVariant,
                        icon = Res.drawable.ic_send,
                    )
                }

                false -> {
                    AppIcon(
                        modifier = Modifier
                            .shadow(1.dp, CircleShape)
                            .background(
                                color = AppTheme.colors.primary,
                                shape = CircleShape
                            )
                            .padding(12.dp),
                        size = 42.dp,
                        tint = AppTheme.colors.onPrimary,
                        icon = Res.drawable.ic_send,
                        onClick = { onAction(ChatUiEvent.OnSendClick) }
                    )
                }
            }
        }

    }
}

@Composable
private fun VoiceBottomBarContent(
    modifier: Modifier,
    message: Message,
    isRecording: Boolean,
    onAction: OnAction
) {
    val isDisableSendIcon by remember(message) { mutableStateOf(message.value.isEmpty()) }

    Row(
        modifier = modifier
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AppIcon(
            modifier = Modifier
                .shadow(1.dp, CircleShape)
                .background(AppTheme.colors.primary, CircleShape)
                .padding(8.dp),
            size = 52.dp,
            tint = AppTheme.colors.onPrimary,
            icon = Res.drawable.ic_keyboard,
            onClick = { onAction(ChatUiEvent.OnChangeScreenInputType(inputType = InputType.Text)) }
        )

        VoiceRecorderAnimatedButton(
            isRecording = isRecording,
            onAction = onAction
        )


        AnimatedContent(isDisableSendIcon) {
            when (it) {
                true -> {
                    AppIcon(
                        modifier = Modifier
                            .shadow(1.dp, CircleShape)
                            .background(
                                color = AppTheme.colors.surfaceContainerHighest,
                                shape = CircleShape
                            )
                            .padding(12.dp),
                        size = 52.dp,
                        tint = AppTheme.colors.outlineVariant,
                        icon = Res.drawable.ic_send,
                    )
                }

                false -> {
                    AppIcon(
                        modifier = Modifier
                            .shadow(1.dp, CircleShape)
                            .background(
                                color = AppTheme.colors.primary,
                                shape = CircleShape
                            )
                            .padding(12.dp),
                        size = 52.dp,
                        tint = AppTheme.colors.onPrimary,
                        icon = Res.drawable.ic_send,
                        onClick = { onAction(ChatUiEvent.OnSendClick) }
                    )
                }
            }
        }
    }
}


@LightDarkPreview
@Composable
private fun TextPreview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {

            TextBottomBarContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                message = Message(value = "Text"),
                onAction = {}
            )

            TextBottomBarContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                message = Message(value = ""),
                onAction = {}
            )
        }
    }
}

@Composable
fun VoiceRecorderAnimatedButton(
    isRecording: Boolean,
    onAction: OnAction,
    modifier: Modifier = Modifier,
) {
    if (isRecording) {
        val infiniteTransition = rememberInfiniteTransition(label = "recording_ripple")
        val ripple1Scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple1Scale"
        )
        val ripple1Alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple1Alpha"
        )
        val ripple2Scale by infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, delayMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple2Scale"
        )
        val ripple2Alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, delayMillis = 600, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "ripple2Alpha"
        )
        val buttonPulse by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "buttonPulse"
        )

        Box(
            modifier = modifier.size(95.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .graphicsLayer {
                        scaleX = ripple1Scale
                        scaleY = ripple1Scale
                        alpha = ripple1Alpha
                    }
                    .background(AppTheme.colors.error.copy(alpha = 0.5f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .graphicsLayer {
                        scaleX = ripple2Scale
                        scaleY = ripple2Scale
                        alpha = ripple2Alpha
                    }
                    .background(AppTheme.colors.error.copy(alpha = 0.5f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(85.dp)
                    .graphicsLayer {
                        scaleX = buttonPulse
                        scaleY = buttonPulse
                    }
                    .shadow(4.dp, CircleShape)
                    .background(AppTheme.colors.error, CircleShape)
                    .clickable { onAction(ChatUiEvent.OnVoiceRecorderClick(false)) },
                contentAlignment = Alignment.Center
            ) {
                LottieLoader(
                    modifier = Modifier.size(52.dp),
                    jsonPath = "files/lt_recording_anim.json",
                    tint = AppTheme.colors.onError
                )
            }
        }
    } else {
        Box(
            modifier = modifier.size(95.dp),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                modifier = Modifier
                    .shadow(1.dp, CircleShape)
                    .background(AppTheme.colors.primary, CircleShape)
                    .padding(16.dp),
                size = 85.dp,
                tint = AppTheme.colors.onPrimary,
                icon = Res.drawable.ic_microphone,
                onClick = { onAction(ChatUiEvent.OnVoiceRecorderClick(true)) }
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun VoicePreview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            VoiceBottomBarContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                message = Message(value = "Text"),
                isRecording = true,
                onAction = {}
            )

            VoiceBottomBarContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                message = Message(value = ""),
                isRecording = false,
                onAction = {}
            )
        }
    }
}