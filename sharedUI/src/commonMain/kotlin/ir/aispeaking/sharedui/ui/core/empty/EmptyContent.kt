package ir.aispeaking.sharedui.ui.core.empty



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmptyContent(
    modifier: Modifier = Modifier.fillMaxWidth(),
    iconSize: Dp = 250.dp,
    icon: DrawableResource = Res.drawable.ic_empty_content,
    text: StringResource = Res.string.empty_content_description,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Image(
            modifier = Modifier
                .testTag("empty-content-image")
                .size(iconSize),
            painter = painterResource(icon),
            contentDescription = "List is empty"
        )

        BodyMediumText(
            modifier = Modifier.testTag("empty-content-text"),
            text = stringResource(text),
            textAlign = TextAlign.Center,
            color = AppTheme.colors.onSurface
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        EmptyContent(modifier = Modifier.fillMaxWidth())
    }
}