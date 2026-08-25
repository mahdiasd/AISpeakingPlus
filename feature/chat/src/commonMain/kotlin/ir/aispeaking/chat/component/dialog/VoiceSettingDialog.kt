package ir.aispeaking.chat.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.menu_ai_voice_setting
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun VoiceSettingDialog(
    voiceSetting: VoiceSetting,
    onAction: OnAction
) {
    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        TitleBoldText(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = stringResource(Res.string.menu_ai_voice_setting)
        )

//        BodyMediumBoldText(
//            text = "${stringResource(Res.string.dialog_speed_title)}: ${"%.1f".format(voiceSetting.speed)}"
//        )

        Slider(
            value = voiceSetting.speed,
            onValueChange = {
                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(speed = it)))
            },
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.primary,
                activeTrackColor = AppTheme.colors.primary,
                inactiveTrackColor = AppTheme.colors.outlineVariant,
                activeTickColor = AppTheme.colors.secondary,
                inactiveTickColor = AppTheme.colors.secondary
            ),
            steps = 16,
            valueRange = 0.5f..2f
        )

//        BodyMediumBoldText(
//            text = "${stringResource(Res.string.dialog_pitch_title)}: ${"%.1f".format(voiceSetting.pitch)}"
//        )

        Slider(
            value = voiceSetting.pitch,
            onValueChange = {
                onAction(ChatUiEvent.OnUpdateVoiceSetting(voiceSetting.copy(pitch = it)))
            },
            colors = SliderDefaults.colors(
                thumbColor = AppTheme.colors.primary,
                activeTrackColor = AppTheme.colors.primary,
                inactiveTrackColor = AppTheme.colors.outlineVariant,
                activeTickColor = AppTheme.colors.secondary,
                inactiveTickColor = AppTheme.colors.secondary
            ),
            steps = 16,
            valueRange = 0.5f..2f
        )
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