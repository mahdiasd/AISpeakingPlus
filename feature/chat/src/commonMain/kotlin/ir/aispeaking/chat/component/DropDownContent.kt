package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.DialogType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_checked
import ir.aispeaking.sharedui.ic_unchecked
import ir.aispeaking.sharedui.ic_voice_model
import ir.aispeaking.sharedui.ic_voice_setting
import ir.aispeaking.sharedui.menu_ai_voice_setting
import ir.aispeaking.sharedui.menu_show_hide_voice_transcript
import ir.aispeaking.sharedui.menu_voice_model
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DropDownContent(
    onAction: OnAction,
    expandedMenu: Boolean,
    voiceSetting: VoiceSetting,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier,
        containerColor = AppTheme.colors.surface,
        expanded = expandedMenu,
        shape = AppTheme.shapes.roundMedium,
        border = BorderStroke(width = 1.dp, color = AppTheme.colors.outlineVariant),
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            text = {
                LabelMediumBoldText(
                    text = stringResource(Res.string.menu_ai_voice_setting),
                    color = AppTheme.colors.onSurface
                )
            },
            trailingIcon = {
                AppIcon(
                    size = 18.dp,
                    icon = Res.drawable.ic_voice_setting,
                    tint = AppTheme.colors.onSurface,
                    onClick = {
                        onAction(ChatUiEvent.OnDialogType(DialogType.VoiceSetting))
                        onDismiss()
                    }
                )
            },
            onClick = {
                onAction(ChatUiEvent.OnDialogType(DialogType.VoiceSetting))
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = {
                LabelMediumBoldText(
                    text = stringResource(Res.string.menu_voice_model),
                    color = AppTheme.colors.onSurface
                )
            },
            trailingIcon = {
                AppIcon(
                    size = 18.dp,
                    icon = Res.drawable.ic_voice_model,
                    tint = AppTheme.colors.onSurface,
                    onClick = {
                        onAction(ChatUiEvent.OnDialogType(DialogType.VoiceList))
                        onDismiss()
                    }
                )
            },
            onClick = {
                onAction(ChatUiEvent.OnDialogType(DialogType.VoiceList))
                onDismiss()
            }
        )

        DropdownMenuItem(
            text = {
                LabelMediumBoldText(
                    modifier = Modifier.animateClickable {
                        onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showTranscribe = !voiceSetting.showTranscribe)))
                    },
                    text = stringResource(Res.string.menu_show_hide_voice_transcript),
                    color = AppTheme.colors.onSurface
                )
            },
            trailingIcon = {
                AnimatedContent(voiceSetting.showTranscribe) {
                    if (it) {
                        AppIcon(
                            size = 18.dp,
                            icon = Res.drawable.ic_checked,
                            tint = AppTheme.colors.primary,
                            onClick = {
                                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showTranscribe = false)))
                                onDismiss()
                            }
                        )
                    } else {
                        AppIcon(
                            size = 18.dp,
                            icon = Res.drawable.ic_unchecked,
                            tint = AppTheme.colors.onSurface,
                            onClick = {
                                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showTranscribe = true)))
                                onDismiss()
                            }
                        )
                    }
                }
            },
            onClick = {
                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showTranscribe = !voiceSetting.showTranscribe)))
                onDismiss()
            }
        )

//        DropdownMenuItem(
//            text = {
//                LabelMediumBoldText(
//                    modifier = Modifier.animateClickable {
//                        onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showSuggests = !voiceSetting.showSuggests)))
//                    },
//                    text = stringResource(Res.string.menu_show_hide_suggest),
//                    color = AppTheme.colors.onSurface
//                )
//            },
//            trailingIcon = {
//                AnimatedContent(voiceSetting.showSuggests) {
//                    if (it) {
//                        AppIcon(
//                            size = 18.dp,
//                            icon = Res.drawable.ic_checked,
//                            tint = AppTheme.colors.primary,
//                            onClick = {
//                                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showSuggests = false)))
//                                onDismiss()
//                            }
//                        )
//                    } else {
//                        AppIcon(
//                            size = 18.dp,
//                            icon = Res.drawable.ic_unchecked,
//                            tint = AppTheme.colors.onSurface,
//                            onClick = {
//                                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showSuggests = true)))
//                                onDismiss()
//                            }
//                        )
//                    }
//                }
//            },
//            onClick = {
//                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(showSuggests = !voiceSetting.showSuggests)))
//                onDismiss()
//            }
//        )

    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        DropDownContent(
            onAction = {  },
            expandedMenu = true,
            voiceSetting = VoiceSetting()
        ) { }
    }
}