package ir.aispeaking.sharedui.ui.core.error


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.*

import ir.aispeaking.sharedui.ui.core.divider.CustomSpacer
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorContent(
    modifier: Modifier = Modifier,
    iconSize: Dp = 250.dp,
    onRetry: () -> Unit,
    icon: DrawableResource = Res.drawable.ic_error_content,
    text: StringResource = Res.string.error_content_description,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Image(
            modifier = Modifier
                .testTag("error-content-image")
                .size(iconSize),
            painter = painterResource(icon),
            contentDescription = "Load list failed"
        )

        CustomSpacer(Modifier.height(0.dp))

        BodyMediumText(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("error-content-text"),
            text = stringResource(text),
            textAlign = TextAlign.Center
        )

        DualContentRow(
            modifier = Modifier.animateClickable(onRetry),
            leftContent = {
                BodyLargeBoldText(
                    modifier = Modifier.animateClickable(onRetry),
                    text = stringResource(Res.string.try_again),
                    color = AppTheme.colors.primary
                )
            },
            rightContent = {
                AppIcon(
                    size = 18.dp,
                    icon = Res.drawable.ic_refresh,
                    tint = AppTheme.colors.primary
                )
            }
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        )
        {
            ErrorContent(modifier = Modifier.fillMaxWidth(), onRetry = {})
        }
    }
}