package ir.aispeaking.chat.component.bubble

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.InputType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.chat.input.Message
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.core.input.textFieldColors
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import ir.aispeaking.sharedui.ui.validation.errorMessage
import ir.aispeaking.sharedui.utils.shape.TooltipShape
import org.jetbrains.compose.resources.stringResource

@Composable
fun BubbleVoiceText(
    modifier: Modifier = Modifier,
    message: Message,
    onAction: OnAction
) {
    val shape = remember {
        TooltipShape(
            arrowWidth = 40.dp,
            arrowHeight = 20.dp,
            cornerRadius = 16.dp
        )
    }

    Column(
        modifier = modifier
//            .shadow(1.dp, shape = shape)
            .background(
                color = AppTheme.colors.surfaceContainerLow,
                shape = shape
            )
            .border(
                width = 1.dp,
                color = AppTheme.colors.outlineVariant,
                shape = shape
            )
            .padding(8.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        AppTextField(
            modifier = Modifier.fillMaxWidth(),
            value = message.value,
            textDirection = TextDirection.Ltr,
            colors = textFieldColors(
                focusedBorderColor = AppTheme.colors.surfaceContainerLow,
                unfocusedBorderColor = AppTheme.colors.surfaceContainerLow,
                disabledBorderColor = AppTheme.colors.surfaceContainerLow,
                errorBorderColor = AppTheme.colors.surfaceContainerLow,
                unfocusedTextColor = AppTheme.colors.onSurface,
                focusedTextColor = AppTheme.colors.onSurface,
                disabledTextColor = AppTheme.colors.onSurface
            ),
            showClearIcon = true,
            maxLines = 6,
            onValueChange = { onAction(ChatUiEvent.OnMessageChange(message = it, inputType = InputType.Text)) },
            hint = stringResource(message.hint),
            errorText = message.validate().errorMessage()?.let { stringResource(it) },
            isError = (message.shouldValidate && message.validate() is ValidationStatus.Invalid),
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        BubbleVoiceText(
            modifier = Modifier.fillMaxWidth(),
            message = Message(value = "This is text for preview")
        ) { }
    }
}