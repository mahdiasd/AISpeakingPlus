package ir.aispeaking.sharedui.ui.core.button



import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.app_name
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ic_arrow_right
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.loading.DotLoading
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppButton(
    modifier: Modifier = Modifier,
    text: StringResource,
    containerColor: Color = AppTheme.colors.primary,
    disabledContainerColor: Color = AppTheme.colors.outline,
    disabled: Boolean = false,
    isLoading: Boolean = false,
    isPersianFont: Boolean = false,
    textColor: Color = AppTheme.colors.onPrimary,
    onClick: () -> Unit,
) {
    Button(
        shape = AppTheme.shapes.roundSmall,
        enabled = !disabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = disabledContainerColor
        ),
        modifier = modifier
            .widthIn(min = 180.dp, max = 220.dp)
            .height(50.dp)
            .then(
                if (disabled) Modifier
                else Modifier
                    .coloredShadow(
                        color = containerColor,
                        borderRadius = 5.dp,
                        shadowRadius = 32.dp,
                        offsetY = 4.dp,
                        alpha = 0.2f
                    )
                    .shadow(2.dp, shape = AppTheme.shapes.roundSmall),
            ),
        onClick = {
            if (!isLoading && !disabled) onClick()
        }) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                DotLoading()
            } else {
                TitleBoldText(
                    persianFont = isPersianFont,
                    text = stringResource(text),
                    color = textColor
                )
            }
        }
    }

}

@Composable
fun AppCompactButton(
    modifier: Modifier = Modifier,
    text: StringResource,
    containerColor: Color = AppTheme.colors.primaryContainer,
    shape: RoundedCornerShape = AppTheme.shapes.roundSmall,
    isLoading: Boolean = false,
    textStyle: TextStyle = AppTheme.typography.bodyMediumBold,
    textColor: Color = AppTheme.colors.onPrimaryContainer,
    loadingDotSize: Dp = 22.dp,
    icon: @Composable (() -> Unit)? = null,
    borderWidth: Dp = 1.dp,
    borderColor: Color = textColor,
    padding: Dp = 12.dp,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
    onClick: () -> Unit,
) {
    AnimatedContent(
        modifier = Modifier
            .animateClickable {
                if (!isLoading) onClick()
            }
            .then(modifier)
            .border(borderWidth, color = borderColor, shape = shape)
            .background(color = containerColor, shape = shape)
            .padding(padding),
        targetState = isLoading) {
        when (it) {
            true -> {
                DotLoading(dotSize = loadingDotSize)
            }

            false -> {
                Row(
                    modifier = Modifier,
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = horizontalArrangement
                ) {
                    Text(
                        text = stringResource(text),
                        color = textColor,
                        style = textStyle,
                        textAlign = TextAlign.Center
                    )

                    icon?.let { iconContent ->
                        iconContent()
                    }
                }
            }
        }
    }
}

@Composable
fun AppBorderButton(
    modifier: Modifier = Modifier,
    text: StringResource,
    shape: RoundedCornerShape = AppTheme.shapes.roundSmall,
    isLoading: Boolean = false,
    textStyle: TextStyle = AppTheme.typography.bodyMediumBold,
    textColor: Color = AppTheme.colors.error,
    borderColor: Color = AppTheme.colors.error,
    loadingDotSize: Dp = 22.dp,
    icon: @Composable (() -> Unit)? = null,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    minWidth: Dp = 85.dp,
    maxWidth: Dp = 200.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally),
    onClick: () -> Unit,
) {
    AnimatedContent(
        modifier = Modifier
            .animateClickable {
                if (!isLoading) onClick()
            }
            .widthIn(min = minWidth, max = maxWidth)
            .border(1.dp, color = borderColor, shape = shape)
            .padding(12.dp)
            .then(modifier),
        targetState = isLoading) {
        when (it) {
            true -> {
                DotLoading(dotSize = loadingDotSize)
            }

            false -> {
                Row(
                    modifier = Modifier,
                    verticalAlignment = verticalAlignment,
                    horizontalArrangement = horizontalArrangement
                ) {
                    Text(
                        text = stringResource(text),
                        color = textColor,
                        style = textStyle,
                        textAlign = TextAlign.Center
                    )

                    icon?.let { iconContent ->
                        iconContent()
                    }
                }
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                32.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            AppButton(text = Res.string.app_name) { }
            AppButton(text = Res.string.app_name, isLoading = true) { }
            AppButton(text = Res.string.app_name, disabled = true) { }
        }
    }
}

@PreviewLightDark
@Composable
private fun CompatPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                32.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            AppCompactButton(
                text = Res.string.app_name,
                icon = {
                    AppIcon(
                        icon = Res.drawable.ic_arrow_right,
                        tint = AppTheme.colors.onPrimaryContainer
                    )
                },
                onClick = { }
            )

        }
    }
}