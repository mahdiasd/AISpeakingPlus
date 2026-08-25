package ir.aispeaking.register.component

import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.congratulations
import ir.aispeaking.sharedui.gift_message
import ir.aispeaking.sharedui.lets_start
import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun GiftDialog(giftDays: Int, moveToMain: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colors.surfaceContainerHighest,
        onDismissRequest = moveToMain,
        title = {
            BodyLargeBoldText(
                text = stringResource( Res.string.congratulations),
                color = AppTheme.colors.success
            )
        },
        text = {
            BodyMediumText(text = stringResource( Res.string.gift_message, giftDays))
        },
        confirmButton = {
            AppCompactButton(
                text = Res.string.lets_start,
                onClick = moveToMain
            )
        }
    )
}

@LightDarkPreview
@Composable
fun GiftDialogPreview() {
    AppTheme {
        GiftDialog(
            giftDays = 7,
            moveToMain = {}
        )
    }
}
