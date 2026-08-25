package ir.aispeaking.sharedui.ui.core.input

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppCompactTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    hint: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = minLines,
    showClearIcon: Boolean = true,
    textDirection: TextDirection = TextDirection.Ltr,
    textAlign: TextAlign = TextAlign.Start,
    shape: RoundedCornerShape = AppTheme.shapes.roundSmall,
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
    errorTextColor: Color = AppTheme.colors.error,
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
    height: Dp = 40.dp, // Custom height parameter
    borderColor: Color = AppTheme.colors.outline,
    focusBorderColor: Color = AppTheme.colors.primary,
    containerColor: Color = AppTheme.colors.surface,
    borderWidth: Dp = 1.dp,
    onFinishTyping: (() -> Unit)? = null
) {
    var hasTyped by remember { mutableStateOf(false) }
    val isFocused = interactionSource.collectIsFocusedAsState().value

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

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .height(height)
                .fillMaxWidth()
                .border(
                    width = borderWidth,
                    color = when {
                        isError -> AppTheme.colors.error
                        isFocused -> focusBorderColor
                        else -> borderColor
                    },
                    shape = shape
                )
                .clip(shape)
                .background(
                    color = containerColor
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .matchParentSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Leading icon
                AnimatedContent(targetState = leadingIcon != null || (showClearIcon && value.isNotEmpty())) { showIcon ->
                    if (showIcon) {
                        if (leadingIcon != null) {
                            Box(Modifier.padding(end = 8.dp)) {
                                leadingIcon()
                            }
                        } else if (showClearIcon && value.isNotEmpty()) {
                            Box(Modifier.padding(end = 8.dp)) {
                                ClearIcon(onClick = { onValueChange("") })
                            }
                        }
                    }
                }

                // Text field content
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = value,
                        onValueChange = {
                            hasTyped = true
                            onValueChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        readOnly = readOnly,
                        textStyle = textStyle.copy(
                            textAlign = textAlign,
                            textDirection = textDirection,
                            color = if (enabled)
                                AppTheme.colors.onSurface
                            else
                                AppTheme.colors.outlineVariant
                        ),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        singleLine = maxLines == 1,
                        maxLines = maxLines,
                        minLines = minLines,
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(AppTheme.colors.primary)
                    )

                    // Placeholder
                    if (value.isEmpty() && hint.isNotEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = hint,
                            style = placeholderTextStyle.copy(
                                textAlign = textAlign,
                                textDirection = textDirection
                            ),
                            color = AppTheme.colors.outline
                        )
                    }
                }

                // Trailing icon
                AnimatedVisibility(visible = trailingIcon != null) {
                    Box(Modifier.padding(start = 8.dp)) {
                        trailingIcon?.invoke()
                    }
                }
            }
        }

        // Error text
        AnimatedVisibility(visible = isError && errorContent != null) {
            errorContent?.invoke()
        }
    }
}
