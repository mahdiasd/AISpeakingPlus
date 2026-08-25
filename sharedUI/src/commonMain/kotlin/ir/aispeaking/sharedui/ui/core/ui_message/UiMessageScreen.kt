package ir.aispeaking.sharedui.ui.core.ui_message

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.model.ui_message.MessageStatus
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.model.ui_message.getMessage
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun UiMessageScreen(
    modifier: Modifier = Modifier,
    shared: SharedFlow<UiMessage?>
) {
    val uiMessage: UiMessage? by shared.collectAsStateWithLifecycle(null)

    AnimatedVisibility(
        modifier = Modifier
            .fillMaxWidth()
            .safeContentPadding(),
        visible = uiMessage != null
    ) {
        MessageBox(
            modifier = Modifier
                .wrapContentWidth()
                .testTag("message-box")
                .background(
                    color = when (uiMessage?.status) {
                        MessageStatus.Success -> AppTheme.colors.success
                        MessageStatus.Failure -> AppTheme.colors.error
                        else -> AppTheme.colors.primary
                    },
                    shape = AppTheme.shapes.roundMedium
                )
                .padding(12.dp)
                .then(modifier),
            textColor = when (uiMessage?.status) {
                MessageStatus.Success -> AppTheme.colors.onSuccess
                MessageStatus.Failure -> AppTheme.colors.onError
                else -> AppTheme.colors.onSurface
            },
            messageText = uiMessage?.content?.getMessage() ?: ""
        )
    }

}

@Composable
fun MessageBox(
    modifier: Modifier,
    messageText: String,
    textColor: Color
) {
    BodyMediumBoldText(
        modifier = modifier,
        text = messageText,
        color = textColor,
        textAlign = TextAlign.Center
    )
}

@LightDarkPreview
@Composable
private fun FailurePreview() {
    AppTheme {
        MessageBox(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = AppTheme.colors.error,
                    shape = AppTheme.shapes.roundMedium
                )
                .padding(12.dp),
            textColor = AppTheme.colors.onError,
            messageText = "This is error message!"
        )
    }
}

@LightDarkPreview
@Composable
private fun SuccessPreview() {
    AppTheme {
        MessageBox(
            modifier = Modifier
                .wrapContentWidth()
                .background(
                    color = AppTheme.colors.success,
                    shape = AppTheme.shapes.roundMedium
                )
                .padding(12.dp),
            textColor = AppTheme.colors.onSuccess,
            messageText = "This is success message!"
        )
    }
}
