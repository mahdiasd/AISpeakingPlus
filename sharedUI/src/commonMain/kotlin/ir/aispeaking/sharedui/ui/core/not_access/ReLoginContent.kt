package ir.aispeaking.sharedui.ui.core.not_access



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.auth
import ir.aispeaking.sharedui.ic_error_content
import ir.aispeaking.sharedui.re_Auth_content_description
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReAuthContent(
    modifier: Modifier = Modifier.fillMaxWidth(),
    iconSize: Dp = 120.dp,
    onAuth: () -> Unit,
    icon: DrawableResource = Res.drawable.ic_error_content,
    text: StringResource = Res.string.re_Auth_content_description,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Image(
            modifier = Modifier
                .testTag("re-Auth-content-image")
                .size(iconSize),
            painter = painterResource(icon),
            contentDescription = "Load list failed"
        )

        LabelMediumText(
            Modifier
                .testTag("re-Auth-content-text"),
            text = stringResource(text),
            color = AppTheme.colors.outline
        )

        LabelMediumText(
            modifier = Modifier.animateClickable(onAuth),
            text = stringResource(Res.string.auth),
            textStyle = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colors.primary
        )
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Surface(modifier = Modifier.baseModifier()) {
            ReAuthContent(modifier = Modifier.fillMaxWidth(), onAuth = {})
        }
    }
}