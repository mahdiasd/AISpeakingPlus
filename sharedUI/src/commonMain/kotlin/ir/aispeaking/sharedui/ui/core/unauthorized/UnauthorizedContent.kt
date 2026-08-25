package ir.aispeaking.sharedui.ui.core.unauthorized

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme


import ir.aispeaking.sharedui.ui.core.button.AppButton
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun UnauthorizedContent(
    modifier: Modifier = Modifier,
    navigateToLogin: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        Image(
            modifier = Modifier.size(200.dp),
            painter =
                painterResource(Res.drawable.login_required),
            contentDescription = ""
        )
        BodyMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.unauthorized_content_title),
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        LabelMediumText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.unauthorized_content_description),
            color = AppTheme.colors.outline,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier)
        Spacer(Modifier)

        AppButton(
            text = Res.string.unauthorized_content_btn,
            onClick = navigateToLogin
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        UnauthorizedContent(
            modifier = Modifier.fillMaxWidth(),
            navigateToLogin = {}
        )
    }
}