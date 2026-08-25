package ir.aispeaking.chat

import androidx.compose.runtime.Stable
import ir.aispeaking.chat.input.Message
import ir.aispeaking.chat.stt.SttUiState
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.socket.SocketStatus
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.domain.model.tts.DEFAULT_KOKORO_VOICES
import ir.aispeaking.domain.model.tts.KokoroVoice
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.ui.model.permission.PermissionState
import ir.aispeaking.sharedui.ui.permission.AppPermission
import ir.aispeaking.sharedui.ui.permission.AudioPermission
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlin.uuid.Uuid

@Stable
data class ChatUiState(
    val screenInputType: InputType = InputType.Voice,
    val message: Message = Message(value = ""),
    val scenario: Scenario? = null,
    val permissions: ImmutableList<AppPermission> = immutableListOf(AudioPermission()),
    val isRecording: Boolean = false,
    val permissionState: PermissionState = PermissionState.Denied,
    val finishedTasksIndex: List<Int> = listOf(),
    val socketStatus: SocketStatus = SocketStatus.Connecting,

    val voices: ImmutableList<KokoroVoice> = DEFAULT_KOKORO_VOICES,
    val selectedVoice: KokoroVoice? = null,
    val voiceSetting: VoiceSetting = VoiceSetting(),

    val dialogType: DialogType = DialogType.None,

    val chats: ImmutableList<Chat> = immutableListOf(),

    val showErrorContent: Boolean = false,
    val guideList: ImmutableList<GuideModel> = immutableListOf(),

    /**
     * Server-side STT state (driven by [SttController]). Empty defaults
     * keep the screen renderable while STT is not active.
     */
    val stt: SttUiState = SttUiState(),
) : UiState


sealed class ChatUiEvent : UiEvent {
    data object OnSendClick : ChatUiEvent()
    data object OnBackClick : ChatUiEvent()
    data object RetrySendChat : ChatUiEvent()

    data object SetGuideRead : ChatUiEvent()

    data object OnSentTasksFinished : ChatUiEvent()

    data object ExitChat : ChatUiEvent()

    data object GetSuggests : ChatUiEvent()

    data class OnChangeAiVoiceState(val chatId: Uuid, val voiceState: AiVoiceState) : ChatUiEvent()
    data class OnVoiceRecorderClick(val start: Boolean) : ChatUiEvent()
    data class OnMessageChange(val message: String, val inputType: InputType) : ChatUiEvent()
    data class OnPermissionChange(val isGrant: Boolean) : ChatUiEvent()
    data class OnChangeScreenInputType(val inputType: InputType) : ChatUiEvent()

    data class OnDialogType(val dialogType: DialogType) : ChatUiEvent()

    data class OnSelectVoice(val voice: KokoroVoice) : ChatUiEvent()

    data class AddToLightener(val translation: Translation?) : ChatUiEvent()

    data class OnUpdateVoiceSetting(val voiceSetting: VoiceSetting) : ChatUiEvent()
}

@Stable
sealed class DialogType {
    data object None : DialogType()

    data object VoiceSetting : DialogType()

    data object VoiceList : DialogType()

    data object ExitChat : DialogType()

    data class FinishedTasks(val finishedTasksState: FinishedTasksState) : DialogType()

    data class SelectText(val text: String, val translation: Translation? = null) : DialogType()
}

sealed class FinishedTasksState {
    data object Loading : FinishedTasksState()
    data object Sent : FinishedTasksState()
    data object Failed : FinishedTasksState()
}

sealed class InputType {
    data object Text : InputType()
    data object Voice : InputType()
}

sealed class ChatUiNavigation : UiNavigation {
    data object ToBack : ChatUiNavigation()
}

typealias OnAction = (ChatUiEvent) -> Unit
