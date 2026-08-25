package ir.aispeaking.englishLevel.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.englishLevel.EnglishLevelUiEvent
import ir.aispeaking.englishLevel.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.english_level_continue_to_register
import ir.aispeaking.sharedui.english_level_result_dialog_description
import ir.aispeaking.sharedui.english_level_result_dialog_show_btn
import ir.aispeaking.sharedui.english_level_result_dialog_title

import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.name
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun ResultDialog(
    languageLevel: LanguageLevel,
    onAction: OnAction
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Content(languageLevel, onAction)
    }
}

@Composable
private fun Content(languageLevel: LanguageLevel, onAction: OnAction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = AppTheme.shapes.roundMedium)
            .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        TitleBoldText(text = stringResource(Res.string.english_level_result_dialog_title))

        BodyMediumText(
            modifier = Modifier,
            text = stringResource(Res.string.english_level_result_dialog_description)
        )

        VerticalSpace()
        TitleBoldText(
            text = stringResource(languageLevel.name()),
            color = AppTheme.colors.primary
        )

        VerticalSpace()

        DualContentRow(
            modifier = Modifier.fillMaxWidth(),
            leftContent = {
                AppCompactButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.9f),
                    textStyle = AppTheme.typography.labelMediumBold,
                    text = Res.string.english_level_result_dialog_show_btn,
                    onClick = { onAction(EnglishLevelUiEvent.OnShowAnswersBtnClick) }
                )
            },
            rightContent = {
                AppCompactButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    containerColor = AppTheme.colors.primary,
                    textColor = AppTheme.colors.onPrimary,
                    textStyle = AppTheme.typography.labelMediumBold,
                    text = Res.string.english_level_continue_to_register,
                    onClick = { onAction(EnglishLevelUiEvent.OnContinueRegisterBtnClick) }
                )
            },
        )
    }
}

@LightDarkPreview
@Composable
fun PreviewContent() {
    AppTheme {
        Content(
            languageLevel = LanguageLevel.B1, // Sample data
            onAction = {} // Empty lambda for preview
        )
    }
}