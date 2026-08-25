package ir.aispeaking.chat.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.chat.ChatUiEvent
import ir.aispeaking.chat.OnAction
import ir.aispeaking.domain.model.tts.DEFAULT_KOKORO_VOICES
import ir.aispeaking.domain.model.tts.KokoroVoice
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.dialog_voice_list_title
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@Composable
fun VoiceListDialog(
    onAction: OnAction,
    ttsVoices: ImmutableList<KokoroVoice>?,
    selectedVoice: KokoroVoice?,
) {
    val listState = rememberLazyListState()
    val voices = ttsVoices ?: DEFAULT_KOKORO_VOICES

    Column(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
            .padding(bottom = 16.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TitleBoldText(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            textAlign = TextAlign.Center,
            text = stringResource(Res.string.dialog_voice_list_title)
        )

        if (voices.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                BodyMediumText(
                    text = "No voices found",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                state = listState
            ) {
                itemsIndexed(
                    items = voices,
                    key = { _, voice -> "${voice.id}_${voice.code}" }
                ) { index, voice ->
                    VoiceItem(
                        voice = voice,
                        isSelected = (selectedVoice?.id == voice.id) || (selectedVoice == null && voice.id == 0),
                        onClick = { onAction(ChatUiEvent.OnSelectVoice(voice)) }
                    )

                    if (index != voices.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            color = AppTheme.colors.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceItem(
    voice: KokoroVoice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateClickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = AppTheme.colors.primary,
                unselectedColor = AppTheme.colors.outline
            )
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BodyMediumBoldText(
                    text = voice.name,
                    color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.onSurface
                )
                LabelSmallBoldText(
                    text = "${voice.gender} • ${voice.accent}",
                    color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            if (voice.description.isNotEmpty()) {
                BodyMediumText(
                    text = voice.description,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun VoiceListDialogPreview() {
    AppTheme {
        VoiceListDialog(
            onAction = {},
            ttsVoices = DEFAULT_KOKORO_VOICES,
            selectedVoice = DEFAULT_KOKORO_VOICES.first()
        )
    }
}
