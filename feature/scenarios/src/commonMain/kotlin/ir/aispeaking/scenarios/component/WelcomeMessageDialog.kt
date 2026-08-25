package ir.aispeaking.scenarios.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.close_dialog_btn
import ir.aispeaking.sharedui.dont_show_again_btn
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyLargeText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeMessageDialog(
    welcomeMessage: WelcomeMessage,
    onDismiss: (dontShowAgain: Boolean) -> Unit,
) {
    var checked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { onDismiss(checked) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            BodyLargeBoldText(
                text = welcomeMessage.title,
                color = AppTheme.colors.primary,
                modifier = Modifier.fillMaxWidth(),
                persianFont = true,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            if (welcomeMessage.imageUrl.isNotEmpty()) {
                AppAsyncImage(
                    data = welcomeMessage.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    shape = AppTheme.shapes.roundMedium
                )
            }

            BodyMediumText(
                text = welcomeMessage.message,
                modifier = Modifier.fillMaxWidth(),
                color = AppTheme.colors.onSurface,
                persianFont = true,
                textAlign = TextAlign.Right
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateClickable { checked = !checked },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LabelMediumText(
                    modifier = Modifier.padding(end = 8.dp),
                    color = AppTheme.colors.onSurface,
                    text = stringResource(Res.string.dont_show_again_btn),
                    persianFont = true
                )

                CustomCheckbox(
                    checked = checked,
                    onCheckedChange = { checked = it }
                )
            }


            BodyLargeText(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = AppTheme.colors.primaryContainer,
                        shape = AppTheme.shapes.roundSmall
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp)
                    .animateClickable { onDismiss(checked) },
                persianFont = true,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onPrimaryContainer,
                text = stringResource(Res.string.close_dialog_btn))
        }
    }
}

@Composable
fun CustomCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (checked) AppTheme.colors.primary else AppTheme.colors.surface,
        label = "checkboxColor"
    )
    val borderColor = if (checked) AppTheme.colors.primary else AppTheme.colors.outline

    Box(
        modifier = modifier
            .size(18.dp)
            .background(color = backgroundColor, shape = RoundedCornerShape(6.dp))
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .animateClickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            AppIcon(
                icon = Res.drawable.ic_done,
                tint = AppTheme.colors.onPrimary,
                size = 14.dp
            )
        }
    }
}

@LightDarkPreview
@Composable
fun WelcomeMessageDialogPreview() {
    AppTheme {
        WelcomeMessageDialog(
            welcomeMessage = WelcomeMessage(
                id = "1",
                title = "خوش آمدید",
                message = "به اپلیکیشن خوش آمدید. امیدواریم تجربه خوبی داشته باشید.",
                imageUrl = ""
            ),
            onDismiss = {}
        )
    }
}
