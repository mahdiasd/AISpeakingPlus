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
import androidx.compose.ui.tooling.preview.Preview
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.them.AppTheme

@Preview
@Composable
private fun TitleTextPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
        ) {
            TitleText(text = "This is TitleText preview")
            TitleBoldText(text = "This is TitleBoldText preview")
        }
    }
}

@Composable
fun TitleText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont : Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.title.copy(fontFamily = AppTheme.typography.persianRegular) else AppTheme.typography.title,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE
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
fun TitleBoldText(
    modifier: Modifier = Modifier,
    text: String,
    persianFont : Boolean = false,
    textStyle: TextStyle = if (persianFont) AppTheme.typography.titleBold.copy(fontFamily = AppTheme.typography.persianBold) else AppTheme.typography.titleBold,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = AppTheme.colors.onSurface,
    textDirection: TextDirection = TextDirection.Ltr,
    maxLines: Int = Int.MAX_VALUE
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