package ir.aispeaking.sharedui.ui.core.text

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import ir.aispeaking.sharedui.ui.them.AppTheme


@Preview
@Composable
private fun BodyTextPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
        ) {
            BodyLargeText(text = "This is BodyLargeText preview")
            BodyLargeBoldText(text = "This is BodyLargeBoldText preview")
            BodyMediumText(text = "This is BodyMediumText preview")
            BodyMediumBoldText(text = "This is BodyMediumBoldText preview")
        }
    }
}

@Composable
fun BodyLargeText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.bodyLarge.copy(fontFamily = AppTheme.typography.persianRegular) else AppTheme.typography.bodyLarge,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = TextDirection.Ltr,
    lineHeight: TextUnit? = null,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        style = textStyle.copy(
            textAlign = textAlign,
            color = color,
            lineHeight = lineHeight ?: textStyle.lineHeight,
            textDirection = textDirection,
        )
    )
}

@Composable
fun BodyLargeBoldText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.bodyLargeBold.copy(fontFamily = AppTheme.typography.persianBold) else AppTheme.typography.bodyLargeBold,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = if (persianFont) TextDirection.Rtl else TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        overflow = overflow,
        minLines = minLines,
        maxLines = maxLines,
        style = textStyle.copy(
            textAlign = textAlign,
            color = color,
            textDirection = textDirection,
        )
    )
}

@Composable
fun BodyMediumText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.bodyMedium.copy(fontFamily = AppTheme.typography.persianRegular) else AppTheme.typography.bodyMedium,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = if (persianFont) TextDirection.Rtl else TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        overflow = overflow,
        style = textStyle.copy(
            textAlign = textAlign,
            color = color,
            textDirection = textDirection,
        )
    )
}

@Composable
fun BodyMediumBoldText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.bodyMediumBold.copy(fontFamily = AppTheme.typography.persianBold) else AppTheme.typography.bodyMediumBold,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = if (persianFont) TextDirection.Rtl else TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        minLines = minLines,
        overflow = overflow,
        style = textStyle.copy(
            textAlign = textAlign,
            color = color,
            textDirection = textDirection,
        )
    )
}

