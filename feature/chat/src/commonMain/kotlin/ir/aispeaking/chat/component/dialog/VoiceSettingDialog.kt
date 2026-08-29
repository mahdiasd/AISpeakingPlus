package ir.aispeaking.chat.component.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.DialogType
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_speaker
import ir.aispeaking.sharedui.ic_voice_setting
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlin.math.abs
import kotlin.math.round

@Composable
fun VoiceSettingDialog(
    voiceSetting: VoiceSetting,
    onAction: OnAction
) {
    val currentSpeed = round(voiceSetting.speed * 10) / 10f
    val currentPitch = round(voiceSetting.pitch * 10) / 10f

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AppTheme.colors.primary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    icon = Res.drawable.ic_voice_setting,
                    tint = AppTheme.colors.primary,
                    size = 24.dp
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                TitleBoldText(
                    text = "تنظیمات صدای هوش مصنوعی",
                    persianFont = true
                )
                BodyMediumText(
                    text = "تنظیم سرعت و زیر و بمی صدای گوینده چت",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.65f),
                    persianFont = true
                )
            }
        }

        // Section 1: Speed Control Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainer, AppTheme.shapes.roundMedium)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppIcon(
                        icon = Res.drawable.ic_speaker,
                        tint = AppTheme.colors.primary,
                        size = 18.dp
                    )
                    BodyMediumBoldText(
                        text = "سرعت پخش صدا",
                        persianFont = true
                    )
                }

                // Value Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = AppTheme.colors.primary.copy(alpha = 0.12f),
                            shape = AppTheme.shapes.roundSmall
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    LabelSmallBoldText(
                        text = "${currentSpeed}x" + if (currentSpeed == 1.0f) " (پیش‌فرض)" else "",
                        color = AppTheme.colors.primary,
                        persianFont = true
                    )
                }
            }

            // Quick Preset Chips for Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val speedPresets = listOf(
                    0.7f to "۰.۷x آهسته",
                    1.0f to "۱.۰x عادی",
                    1.2f to "۱.۲x سریع",
                    1.5f to "۱.۵x تند"
                )
                speedPresets.forEach { (presetSpeed, label) ->
                    val isSelected = abs(currentSpeed - presetSpeed) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(AppTheme.shapes.roundSmall)
                            .background(
                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.surfaceContainerLow
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.outlineVariant.copy(alpha = 0.5f),
                                shape = AppTheme.shapes.roundSmall
                            )
                            .animateClickable {
                                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(speed = presetSpeed)))
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LabelSmallBoldText(
                            text = label,
                            color = if (isSelected) AppTheme.colors.onPrimary else AppTheme.colors.onSurface,
                            persianFont = true,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Slider(
                value = voiceSetting.speed,
                onValueChange = {
                    val rounded = round(it * 10) / 10f
                    onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(speed = rounded)))
                },
                colors = SliderDefaults.colors(
                    thumbColor = AppTheme.colors.primary,
                    activeTrackColor = AppTheme.colors.primary,
                    inactiveTrackColor = AppTheme.colors.outlineVariant,
                    activeTickColor = AppTheme.colors.secondary,
                    inactiveTickColor = AppTheme.colors.secondary
                ),
                steps = 14,
                valueRange = 0.5f..2.0f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelSmallBoldText(
                    text = "۰.۵x (بسیار آهسته)",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.5f),
                    persianFont = true
                )
                LabelSmallBoldText(
                    text = "۲.۰x (بسیار سریع)",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.5f),
                    persianFont = true
                )
            }
        }

        // Section 2: Pitch Control Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainer, AppTheme.shapes.roundMedium)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                BodyMediumBoldText(
                    text = "زیر و بمی صدا (Pitch)",
                    persianFont = true
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = AppTheme.colors.secondary.copy(alpha = 0.12f),
                            shape = AppTheme.shapes.roundSmall
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    LabelSmallBoldText(
                        text = "${currentPitch}x" + if (currentPitch == 1.0f) " (استاندارد)" else "",
                        color = AppTheme.colors.secondary,
                        persianFont = true
                    )
                }
            }

            // Quick Preset Chips for Pitch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val pitchPresets = listOf(
                    0.8f to "۰.۸x بم‌تر",
                    1.0f to "۱.۰x استاندارد",
                    1.2f to "۱.۲x زیرتر"
                )
                pitchPresets.forEach { (presetPitch, label) ->
                    val isSelected = abs(currentPitch - presetPitch) < 0.05f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(AppTheme.shapes.roundSmall)
                            .background(
                                color = if (isSelected) AppTheme.colors.secondary else AppTheme.colors.surfaceContainerLow
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AppTheme.colors.secondary else AppTheme.colors.outlineVariant.copy(alpha = 0.5f),
                                shape = AppTheme.shapes.roundSmall
                            )
                            .animateClickable {
                                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(pitch = presetPitch)))
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LabelSmallBoldText(
                            text = label,
                            color = if (isSelected) AppTheme.colors.onSecondary else AppTheme.colors.onSurface,
                            persianFont = true,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Slider(
                value = voiceSetting.pitch,
                onValueChange = {
                    val rounded = round(it * 10) / 10f
                    onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(pitch = rounded)))
                },
                colors = SliderDefaults.colors(
                    thumbColor = AppTheme.colors.secondary,
                    activeTrackColor = AppTheme.colors.secondary,
                    inactiveTrackColor = AppTheme.colors.outlineVariant,
                    activeTickColor = AppTheme.colors.primary,
                    inactiveTickColor = AppTheme.colors.primary
                ),
                steps = 14,
                valueRange = 0.5f..2.0f
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabelSmallBoldText(
                    text = "۰.۵x (صدای بم)",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.5f),
                    persianFont = true
                )
                LabelSmallBoldText(
                    text = "۲.۰x (صدای زیر)",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.5f),
                    persianFont = true
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Confirm / Close Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.roundMedium)
                .background(AppTheme.colors.primary)
                .animateClickable {
                    onAction(ChatUiEvent.OnDialogType(DialogType.None))
                }
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            BodyMediumBoldText(
                text = "تایید و بستن",
                color = AppTheme.colors.onPrimary,
                persianFont = true
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        VoiceSettingDialog(
            voiceSetting = VoiceSetting()
        ) { }
    }
}