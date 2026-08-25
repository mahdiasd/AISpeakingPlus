package ir.aispeaking.chat.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.DialogType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.model.tts.DEFAULT_KOKORO_VOICES
import ir.aispeaking.domain.model.tts.KokoroVoice
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.exit
import ir.aispeaking.sharedui.exit_dialog_message
import ir.aispeaking.sharedui.exit_dialog_title
import ir.aispeaking.sharedui.not_now
import ir.aispeaking.sharedui.ui.core.dialog.MessageDialog
import ir.aispeaking.sharedui.ui.core.dialog.translate.TranslateDialog
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandleDialogs(
    dialogType: DialogType,
    onAction: OnAction,
    voiceSetting: VoiceSetting,
    voices: ImmutableList<KokoroVoice>? = DEFAULT_KOKORO_VOICES,
    selectedVoice: KokoroVoice? = null,
) {
    when (dialogType) {
        DialogType.None -> {}

        DialogType.VoiceSetting -> {
            ModalBottomSheet(
                onDismissRequest = { onAction(ChatUiEvent.OnDialogType(DialogType.None)) },
                containerColor = AppTheme.colors.surfaceContainerLow,
                content = {
                    VoiceSettingDialog(
                        voiceSetting = voiceSetting,
                        onAction = onAction
                    )
                })
        }

        DialogType.VoiceList -> {
            val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { onAction(ChatUiEvent.OnDialogType(DialogType.None)) },
                sheetState = sheetState,
                containerColor = AppTheme.colors.surfaceContainerLow,
                content = {
                    VoiceListDialog(
                        ttsVoices = voices,
                        selectedVoice = selectedVoice,
                        onAction = { event ->
                            onAction(event)
                            if (event is ChatUiEvent.OnSelectVoice) {
                                onAction(ChatUiEvent.OnDialogType(DialogType.None))
                            }
                        }
                    )
                })
        }

        is DialogType.SelectText -> {
            TranslateDialog(
                modifier = Modifier,
                text = dialogType.text,
                onDismiss = { onAction(ChatUiEvent.OnDialogType(DialogType.None)) },
            )
        }

        is DialogType.FinishedTasks -> {
            Dialog(
                onDismissRequest = { onAction(ChatUiEvent.OnDialogType(DialogType.None)) },
                content = {
                    TasksFinishedDialog(finishedTasksState = dialogType.finishedTasksState, onAction = onAction)
                }
            )
        }

        DialogType.ExitChat -> {
            MessageDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                    .padding(16.dp),
                title = stringResource(Res.string.exit_dialog_title),
                message = stringResource(Res.string.exit_dialog_message),
                positiveText = Res.string.not_now,
                negativeText = Res.string.exit,
                onPositive = {
                    onAction(ChatUiEvent.OnDialogType(DialogType.None))
                },
                onNegative = {
                    onAction(ChatUiEvent.OnBackClick)
                    onAction(ChatUiEvent.OnDialogType(DialogType.None))
                },
                onDismiss = {
                    onAction(ChatUiEvent.OnDialogType(DialogType.None))
                }
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun HandleDialogsExitChatPreview() {
    AppTheme {
        HandleDialogs(
            dialogType = DialogType.ExitChat,
            onAction = {},
            voiceSetting = VoiceSetting()
        )
    }
}

@PreviewLightDark
@Composable
private fun HandleDialogsVoiceSettingPreview() {
    AppTheme {
        HandleDialogs(
            dialogType = DialogType.VoiceSetting,
            onAction = {},
            voiceSetting = VoiceSetting()
        )
    }
}
