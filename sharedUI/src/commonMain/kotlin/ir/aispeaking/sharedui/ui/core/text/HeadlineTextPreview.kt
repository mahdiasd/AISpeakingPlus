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
import ir.aispeaking.sharedui.ui.them.AppTheme

@Preview
@Composable
private fun HeadlineTextPreview() {
    AppTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surface)
        ) {
            HeadlineText(text = "This is HeadlineText preview")
            HeadlineBoldText(text = "This is HeadlineBoldText preview")
        }
    }
}

@Composable
fun HeadlineText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = AppTheme.typography.headline,
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
fun HeadlineBoldText(
    modifier: Modifier = Modifier,
    text: String,
    textStyle: TextStyle = AppTheme.typography.headlineBold,
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