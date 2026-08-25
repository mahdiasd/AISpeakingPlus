package ir.aispeaking.sharedui.ui.core.dialog



import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.core.button.AppBorderButton
import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun MessageDialog(
    modifier: Modifier,
    title: String,
    message: String,
     positiveText: StringResource,
     negativeText: StringResource?,
    properties: DialogProperties = DialogProperties(),
     image: DrawableResource? = null,
    onPositive: () -> Unit,
    onNegative: () -> Unit,
    onDismiss: () -> Unit,
) {

    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        DialogContent(
            modifier = modifier,
            title = title,
            message = message,
            positiveText = positiveText,
            negativeText = negativeText,
            onPositive = onPositive,
            image = image,
            onNegative = onNegative,
        )
    }

}

@Composable
fun DialogContent(
    modifier: Modifier = Modifier,
    title: String,
    message: String,
     positiveText: StringResource,
     negativeText: StringResource?,
     image: DrawableResource? = null,
    onPositive: () -> Unit,
    onNegative: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterVertically)
    ) {
        BodyLargeBoldText(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = title
        )

        image?.let {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(it),
                contentDescription = ""
            )
        }
        BodyMediumText(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = message
        )

        DualContentRow(
            modifier = Modifier
                .fillMaxWidth(.9f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            leftContent = {
                negativeText?.let {
                    AppBorderButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = negativeText,
                        onClick = onNegative
                    )
                } ?: run {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }
            },
            rightContent = {
                AppCompactButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    text = positiveText,
                    onClick = onPositive
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        {
            DialogContent(
                title = "This is Title",
                message = "This is message",
                positiveText = Res.string.app_name,
                negativeText = Res.string.app_name,
                image = Res.drawable.dialog_message_vector,
                onPositive = {},
                onNegative = {}
            )
        }
    }
}
