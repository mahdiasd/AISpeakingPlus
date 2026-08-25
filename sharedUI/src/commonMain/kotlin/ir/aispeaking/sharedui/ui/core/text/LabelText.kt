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
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.them.AppTheme

@Preview
@Composable
private fun LabelTextPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
        ) {
            LabelSmallText(text = "This is LabelSmallText preview")
            LabelSmallBoldText(text = "This is LabelSmallBoldText preview")
            LabelMediumText(text = "This is LabelMediumText preview")
            LabelMediumBoldText(text = "This is LabelMediumBoldText preview")
        }
    }
}

@Composable
fun LabelSmallText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.labelSmall.copy(fontFamily = AppTheme.typography.persianRegular) else AppTheme.typography.labelSmall,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = if (persianFont) TextDirection.Rtl else TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE,
) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = maxLines,
        style = textStyle.copy(
            textAlign = textAlign,
            color = color,
            textDirection = textDirection,
        )
    )
}

@Composable
fun LabelSmallBoldText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.labelSmallBold.copy(fontFamily = AppTheme.typography.persianBold) else AppTheme.typography.labelSmallBold,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = if (persianFont) TextDirection.Rtl else TextDirection.Ltr,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE,
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
fun LabelMediumText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.labelMedium.copy(fontFamily = AppTheme.typography.persianRegular) else AppTheme.typography.labelMedium,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = if (persianFont) TextDirection.Rtl else TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    minLines: Int = 1,
) {
    Text(
        modifier = modifier,
        text = text,
        minLines = minLines,
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
fun LabelMediumBoldText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont: Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.labelMediumBold.copy(fontFamily = AppTheme.typography.persianBold) else AppTheme.typography.labelMediumBold,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = TextDirection.Ltr,
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