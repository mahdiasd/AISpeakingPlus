package ir.aispeaking.sharedui.ui.core.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_setting
import ir.aispeaking.sharedui.is_search
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.extension.iconSize
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource


@Composable
fun textFieldColors(
    focusedTextColor: Color = AppTheme.colors.onSurface,
    unfocusedTextColor: Color = AppTheme.colors.onSurface,
    disabledTextColor: Color = AppTheme.colors.outlineVariant,
    focusedBorderColor: Color = AppTheme.colors.outline,
    unfocusedBorderColor: Color = AppTheme.colors.outlineVariant,
    disabledBorderColor: Color = AppTheme.colors.outlineVariant,
    errorBorderColor: Color = AppTheme.colors.error,
    errorContainerColor: Color = Color.Transparent,
    focusedContainerColor: Color = Color.Transparent,
    unfocusedContainerColor: Color = Color.Transparent,
    disabledContainerColor: Color = Color.Transparent
) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = focusedTextColor,
    unfocusedTextColor = unfocusedTextColor,
    disabledTextColor = disabledTextColor,
    focusedBorderColor = focusedBorderColor,
    unfocusedBorderColor = unfocusedBorderColor,
    disabledBorderColor = disabledBorderColor,
    errorBorderColor = errorBorderColor,
    errorContainerColor = errorContainerColor,
    focusedContainerColor = focusedContainerColor,
    unfocusedContainerColor = unfocusedContainerColor,
    disabledContainerColor = disabledContainerColor
)


@Composable
fun AppTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1,
    errorTextColor: Color = AppTheme.colors.error,
    maxLines: Int = minLines,
    showClearIcon: Boolean = true,
    textDirection: TextDirection = TextDirection.Ltr,
    textAlign: TextAlign = TextAlign.Start,
    shape: RoundedCornerShape = AppTheme.shapes.roundSmall,
    colors: TextFieldColors = textFieldColors(),
    textStyle: TextStyle = AppTheme.typography.bodyMedium,
    placeholderTextStyle: TextStyle = AppTheme.typography.bodyMedium.copy(
        color = AppTheme.colors.outlineVariant,
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorText: String? = null,
    errorContent: @Composable (() -> Unit)? = errorText?.let {
        {
            LabelSmallBoldText(
                text = errorText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = textAlign,
                color = errorTextColor
            )
        }
    },
    placeholder: @Composable (() -> Unit)? = {
        Text(
            text = hint,
            modifier = Modifier.fillMaxWidth(),
            textAlign = textAlign,
            style = placeholderTextStyle.copy(textAlign = textAlign, textDirection = textDirection),
            color = AppTheme.colors.outline
        )
    },
    clearIcon: @Composable (() -> Unit)? = if (showClearIcon && value.isNotEmpty()) {
        {
            ClearIcon(
                onClick = { onValueChange("") }
            )
        }
    } else null,
    onFinishTyping: (() -> Unit)? = null
) {

    var hasTyped by remember { mutableStateOf(false) }

    if (onFinishTyping != null) {
        var idleJob by remember { mutableStateOf<Job?>(null) }

        LaunchedEffect(value) {
            idleJob?.cancel()
            idleJob = launch {
                delay(1500)
                onFinishTyping()
            }
        }
    }


    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            hasTyped = true
            onValueChange(it)
        },
        enabled = enabled,
        readOnly = readOnly,
        textStyle = textStyle.copy(textAlign = textAlign, textDirection = textDirection),
        placeholder = placeholder,
        isError = isError,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        interactionSource = interactionSource,
        shape = shape,
        colors = colors,
        maxLines = maxLines,
        minLines = minLines,
        leadingIcon = leadingIcon ?: clearIcon,
        trailingIcon = trailingIcon,
        supportingText = errorContent
    )
}


@Composable
internal fun ClearIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    size: Dp = 24.dp,
) {
    Icon(
        modifier = modifier
            .testTag("text-field-clear")
            .iconSize(size)
            .background(AppTheme.colors.primary, CircleShape)
            .padding(4.dp)
            .clickable(onClick = onClick),
        painter = painterResource(Res.drawable.ic_close),
        contentDescription = "Clear text",
        tint = AppTheme.colors.onPrimary
    )
}


/*-------------------- Previews ---------------------------------*/

@PreviewLightDark
@Composable
private fun AppTextFieldPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Normal state
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "",
                onValueChange = {},
                hint = "Enter text"
            )

            // With value
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "Sample text",
                onValueChange = {},
                hint = "Enter text"
            )

            // With error
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "Invalid input",
                onValueChange = {},
                hint = "Enter text",
                isError = true,
                errorText = "This field is required"
            )

            // Disabled state
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "Disabled text",
                onValueChange = {},
                hint = "Enter text",
                enabled = false
            )


            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "",
                onValueChange = {},
                hint = "Hint text",
                enabled = false
            )

            // With custom icons
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "With icons",
                onValueChange = {},
                hint = "Search",
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.is_search),
                        contentDescription = null,
                        tint = AppTheme.colors.onSurface
                    )
                },
                trailingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_setting),
                        contentDescription = null,
                        tint = AppTheme.colors.onSurface
                    )
                }
            )

            // Multiline
            AppTextField(
                modifier = Modifier.fillMaxWidth(),
                value = "This is a multiline text field with multiple lines of text to show how it handles wrapping.",
                onValueChange = {},
                hint = "Enter multiple lines",
                maxLines = 3
            )
        }
    }
}
