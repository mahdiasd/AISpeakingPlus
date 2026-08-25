package ir.aispeaking.chat

import androidx.lifecycle.viewModelScope
import ir.aispeaking.chat.stt.SttController
import ir.aispeaking.chat.stt.SttUiState
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.domain.model.tts.DEFAULT_KOKORO_VOICES
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.domain.usecase.challenge_progress.CreateChallengeProgressUseCase
import ir.aispeaking.domain.usecase.chat.GetChatSuggestionsUseCase
import ir.aispeaking.domain.usecase.chat.SendChatUseCase
import ir.aispeaking.domain.usecase.guide.ReadGuideStatusUseCase
import ir.aispeaking.domain.usecase.guide.SetGuideForChat
import ir.aispeaking.domain.usecase.scenario.GetLocalScenarioUseCase
import ir.aispeaking.domain.usecase.scenario_progress.CreateScenarioProgressUseCase
import ir.aispeaking.domain.usecase.translation.CreateTranslateUseCase
import ir.aispeaking.domain.usecase.translation.TranslateUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.domain.usecase.voice_setting.ReadVoiceSettingUseCase
import ir.aispeaking.domain.usecase.voice_setting.SaveVoiceSettingUseCase
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_local_scenario_not_found
import ir.aispeaking.sharedui.error_local_user_not_found
import ir.aispeaking.sharedui.error_translation
import ir.aispeaking.sharedui.translations_created
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.permission.PermissionState
import ir.aispeaking.sharedui.ui.model.ui_message.MessageStatus
import ir.aispeaking.sharedui.ui.model.ui_message.MessageType
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.constant.AppConstant
import ir.aispeaking.utils.dLog
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.time.Duration.Companion.milliseconds

@KoinViewModel
class ChatViewModel(
    private val level: String,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
    private val getLocalScenarioUseCase: GetLocalScenarioUseCase,

    private val sendChatUseCase: SendChatUseCase,

    private val readVoiceSettingUseCase: ReadVoiceSettingUseCase,
    private val saveVoiceSettingUseCase: SaveVoiceSettingUseCase,
    private val translateUseCase: TranslateUseCase,

    private val createScenarioProgressUseCase: CreateScenarioProgressUseCase,
    private val createChallengeProgressUseCase: CreateChallengeProgressUseCase,

    private val createTranslateUseCase: CreateTranslateUseCase,
    private val getChatSuggestionsUseCase: GetChatSuggestionsUseCase,
    private val readGuideStatusUseCase: ReadGuideStatusUseCase,
    private val setGuideForChat: SetGuideForChat,

    private val sttController: SttController,
) : BaseViewModel<ChatUiState, ChatUiEvent>() {
    private var user: User? = null
    private val userLanguageGroupLevel: String = level

    init {
        viewModelScope.launch {
            getLocalScenario()
            getLocalUser()
            readVoiceSettingUseCase()?.let { savedSetting ->
                val matchedVoice = DEFAULT_KOKORO_VOICES.find { it.id == savedSetting.voiceId } ?: DEFAULT_KOKORO_VOICES.first()
                setState {
                    copy(
                        voiceSetting = savedSetting,
                        selectedVoice = matchedVoice
                    )
                }
            } ?: run {
                setState {
                    copy(selectedVoice = DEFAULT_KOKORO_VOICES.first())
                }
            }
        }
        // Mirror SttController's UI state into ChatUiState so the chat
        // screen can render the live transcript + errors in one place.
        viewModelScope.launch {
            sttController.uiState.collect { sttState ->
                setState { copy(stt = sttState, isRecording = sttState.isRecording) }
            }
        }
        viewModelScope.launch {
            sttController.events.collect { event ->
                when (event) {
                    is ir.aispeaking.chat.stt.SttEvent.FinalTranscript -> {
                        val formatted = formatSpeechText(event.text)
                        val current = currentState.message.value.trim()
                        val combined = if (current.isBlank()) formatted else "$current $formatted"
                        setState {
                            copy(message = currentState.message.copy(value = combined, shouldValidate = false))
                        }
                    }
                    is ir.aispeaking.chat.stt.SttEvent.Error -> {
                        setState { copy(isRecording = false) }
                        val displayMsg = if (event.message == "server_busy") {
                            "Server is busy. Please try again in a moment."
                        } else {
                            event.message
                        }
                        setUiMessage(
                            UiMessage(
                                stringValue = displayMsg,
                                status = MessageStatus.Failure,
                                messageType = MessageType.Device
                            )
                        )
                    }
                    is ir.aispeaking.chat.stt.SttEvent.SessionStarted -> {
                        setState { copy(isRecording = true) }
                    }
                    is ir.aispeaking.chat.stt.SttEvent.SessionStopped -> {
                        setState { copy(isRecording = false) }
                    }
                }
            }
        }
    }


    override fun onTriggerEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.OnBackClick -> {
                setUiNavigation(ChatUiNavigation.ToBack)
            }

            is ChatUiEvent.OnSendClick -> {
                if (currentState.isRecording) {
                    sttController.stop()
                    setState { copy(isRecording = false) }
                }
                if (validate()) {
                    setState {
                        copy(
                            chats = currentState.chats.toMutableList().apply {
                                add(
                                    Chat.User(
                                        message = currentState.message.value,
                                        status = ChatStatus.Sending
                                    )
                                )
                            }.toImmutableList()
                        )
                    }
                    sendMessage()
                }
            }

            is ChatUiEvent.OnChangeScreenInputType -> setState { copy(screenInputType = event.inputType) }

            is ChatUiEvent.OnMessageChange -> {
                setState {
                    copy(
                        message = currentState.message.copy(value = event.message, shouldValidate = false)
                    )
                }
            }

            is ChatUiEvent.OnVoiceRecorderClick -> {
                if (!event.start) {
                    sttController.stop()
                    setState { copy(isRecording = false) }
                } else if (currentState.permissionState is PermissionState.Granted || isWebTarget()) {
                    setState { copy(isRecording = true) }
                    // Browser target (JS / WASM) cannot set the Authorization
                    // header on a raw WebSocket; it must send the JWT as
                    // ?token=... in the query string instead.
                    val tokenInQuery = isWebTarget()
                    sttController.start(passTokenInQuery = tokenInQuery)
                } else {
                    setState { copy(permissionState = PermissionState.Requesting) }
                }
            }

            is ChatUiEvent.OnPermissionChange -> {
                setState {
                    copy(
                        permissionState = when (event.isGrant) {
                            true -> PermissionState.Granted
                            false -> PermissionState.Denied
                        }
                    )
                }
            }

            is ChatUiEvent.RetrySendChat -> {
                if (currentState.chats.isEmpty() && currentState.scenario?.starter == Role.Model) {
                    startWithAi()
                } else {
                    currentState.chats.lastOrNull()?.takeIf { it is Chat.User }?.let { lastChat ->
                        setState {
                            copy(
                                message = currentState.message.copy(value = (lastChat as Chat.User).message),
                                chats = currentState.chats.mapNotNull { if (it.uid == lastChat.uid) null else it }.toImmutableList()
                            )
                        }
                        onTriggerEvent(ChatUiEvent.OnSendClick)
                    }
                }
            }

            is ChatUiEvent.OnChangeAiVoiceState -> {
                setState {
                    copy(
                        chats = chats.map { m ->
                            if (m is Chat.Ai && m.uid == event.chatId) m.copy(voiceState = event.voiceState)
                            else if (m is Chat.Ai) m.copy(voiceState = AiVoiceState.Stopped)
                            else m
                        }.toImmutableList()
                    )
                }
            }

            is ChatUiEvent.OnDialogType -> {
                if (event.dialogType is DialogType.SelectText && event.dialogType.text.isNotEmpty()) {
                    event.dialogType.text.dLog("text=")
                    setState { copy(dialogType = event.dialogType) }
                    translate(event.dialogType.text)
                } else {
                    setState { copy(dialogType = event.dialogType) }
                }
            }

            is ChatUiEvent.AddToLightener -> {
                currentState.dialogType
                    .takeIf { it is DialogType.SelectText && it.translation != null }?.let {
                        createTranslation((it as DialogType.SelectText).translation!!)
                    }
            }

            is ChatUiEvent.OnSentTasksFinished -> {
                sendTasksFinished()
            }

            is ChatUiEvent.ExitChat -> {
                setUiNavigation(ChatUiNavigation.ToBack)
            }

            is ChatUiEvent.GetSuggests -> {
                getSuggest()
            }

            is ChatUiEvent.OnUpdateVoiceSetting -> {
                val matchedVoice = currentState.voices.find { it.id == event.voiceSetting.voiceId } ?: currentState.selectedVoice
                setState {
                    copy(
                        voiceSetting = event.voiceSetting,
                        selectedVoice = matchedVoice
                    )
                }
                saveVoiceSetting(event.voiceSetting)
            }

            is ChatUiEvent.OnSelectVoice -> {
                val updatedSetting = currentState.voiceSetting.copy(voiceId = event.voice.id)
                setState {
                    copy(
                        selectedVoice = event.voice,
                        voiceSetting = updatedSetting
                    )
                }
                saveVoiceSetting(updatedSetting)
            }

            is ChatUiEvent.SetGuideRead -> {
            }
        }
    }

    private fun getLocalUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect { dataResult ->
                dataResult.onSuccess {
                    user = it
                }.onFailure {
                    setUiMessage(UiMessage(intValue = Res.string.error_local_user_not_found))
                }
            }
        }
    }

    private fun getLocalScenario() {
        viewModelScope.launch {
            getLocalScenarioUseCase()?.let {
                setState { copy(scenario = it) }
                if (it.starter != Role.User) {
                    startWithAi()
                }
            } ?: run {
                setUiMessage(UiMessage(intValue = Res.string.error_local_scenario_not_found))
                delay(2000.milliseconds)
                setUiNavigation(ChatUiNavigation.ToBack)
            }
        }
    }

    private fun startWithAi() {
        if (currentState.chats.isEmpty() && currentState.scenario?.starter != Role.User) {
            setState {
                copy(chats = chats.toMutableList().apply { add(Chat.WaitingForAi) }.toImmutableList())
            }
            sendMessage()
        }
    }

    private fun getSuggest() {
        val scenario = currentState.scenario ?: return
        val lastChat: Chat.Ai = currentState.chats.findLast { it is Chat.Ai } as? Chat.Ai? ?: return
        setState {
            copy(chats = chats.map { chat ->
                if (chat.uid == lastChat.uid) (chat as Chat.Ai).copy(fetchingSuggest = true)
                else chat
            }.toImmutableList())
        }
        viewModelScope.launch {
            getChatSuggestionsUseCase.invoke(
                lastAiMessage = lastChat.message,
                tasks = scenario.tasks.filter { !it.finished }.map { it.description },
                englishLevel = user?.languageLevel?.name ?: LanguageLevel.A1.name,
                scenarioDescription = scenario.description,
            ).collect {
                it.onSuccess { suggests ->
                    suggests.dLog("vm:")
                    setState {
                        copy(chats = chats.map { chat ->
                            if (chat.uid == lastChat.uid) {
                                (chat as Chat.Ai).copy(suggests = suggests.toImmutableList(), fetchingSuggest = false)
                            } else {
                                chat
                            }
                        }.toImmutableList())
                    }
                    currentState.chats.lastOrNull().dLog("chats:")
                }.onFailure { apiError ->
                    setUiMessage(apiError.toUiMessage())
                    setState {
                        copy(chats = chats.map { chat ->
                            if (chat.uid == lastChat.uid) (chat as Chat.Ai).copy(fetchingSuggest = false)
                            else chat
                        }.toImmutableList())
                    }
                }
            }
        }
    }

    private fun saveVoiceSetting(voiceSetting: VoiceSetting) {
        viewModelScope.launch {
            saveVoiceSettingUseCase.invoke(voiceSetting)
        }
    }

    override fun createInitialState() = ChatUiState()

    private fun sendMessage() {
        val message = currentState.message.value
        setState { copy(message = currentState.message.copy(value = "", shouldValidate = false)) }

        val isFirstMessage = when (currentState.scenario!!.starter) {
            Role.System, Role.Model -> currentState.chats.filterIsInstance<Chat.Ai>().size <= 1
            Role.User -> currentState.chats.filterIsInstance<Chat.User>().size <= 1
        }

        viewModelScope.launch {
            sendChatUseCase.invoke(
                scenarioId = currentState.scenario!!.id,
                message = message,
                starter = currentState.scenario!!.starter,
                isChallenge = currentState.scenario!!.isChallenge,
                isFirstMessage = isFirstMessage,
                englishLevel = userLanguageGroupLevel,
                voiceId = currentState.voiceSetting.voiceId,
                generateAudio = true
            ).collect {
                collectChatResponse(it)
            }
        }
    }

    private suspend fun collectChatResponse(dataResult: DataResult<Chat>) {
        dataResult
            .onSuccess { chat ->
                updatedFinishedTasks(chat as Chat.Ai)
                updateUserChat(chat)
                setState {
                    copy(
                        chats = chats.filterNot { it is Chat.WaitingForAi }
                            .toMutableList()
                            .apply { add(chat) }
                            .toImmutableList(),
                        showErrorContent = false
                    )
                }
            }.onFailure { apiError ->
                setUiMessage(apiError.toUiMessage())
                val chats = currentState.chats
                    .filterNot { it is Chat.WaitingForAi }
                    .map {
                        if (it is Chat.User && it.status is ChatStatus.Sending) {
                            it.copy(status = ChatStatus.Failed)
                        } else it
                    }.toImmutableList()

                setState {
                    copy(
                        chats = chats,
                        showErrorContent = chats.isEmpty()
                    )
                }
            }
    }

    private fun updateUserChat(aiChat: Chat.Ai) {
        viewModelScope.launch {
            val lastChatId = currentState.chats.findLast { it is Chat.User }?.uid
            setState {
                copy(chats = currentState.chats.map { chat ->
                    if (chat is Chat.User && chat.uid == lastChatId) {
                        chat.copy(status = ChatStatus.Answered(aiChat.grammar))
                    } else chat
                }.toImmutableList())
            }
        }
    }

    private fun updatedFinishedTasks(chat: Chat.Ai) {
        if (chat.finishTaskIndexes.isEmpty()) return
        viewModelScope.launch {
            currentState.scenario?.let {
                setState {
                    copy(
                        scenario = it.copy(
                            tasks = it.tasks.mapIndexed { index, scenarioTask ->
                                if (index in chat.finishTaskIndexes) scenarioTask.copy(finished = true)
                                else scenarioTask
                            }.toImmutableList()
                        )
                    )
                }
            }
            if (currentState.scenario?.tasks?.all { it.finished } == true) {
                delay(3000)
                AppConstant.needToRefreshScenarioDetail = true
                onTriggerEvent(ChatUiEvent.OnSentTasksFinished)
            }
        }
    }

    private fun sendTasksFinished() {
        viewModelScope.launch {
            onTriggerEvent(ChatUiEvent.OnDialogType(DialogType.FinishedTasks(FinishedTasksState.Loading)))
            when (currentState.scenario!!.isChallenge) {
                true -> {
                    createChallengeProgressUseCase(
                        challengeId = currentState.scenario!!.id,
                        score = currentState.scenario!!.score
                    )
                }

                false -> {
                    createScenarioProgressUseCase(
                        scenarioId = currentState.scenario!!.id,
                        score = currentState.scenario!!.score
                    )
                }
            }.collect {
                it.onSuccess {
                    onTriggerEvent(ChatUiEvent.OnDialogType(DialogType.FinishedTasks(FinishedTasksState.Sent)))
                }.onFailure { apiError ->
                    onTriggerEvent(ChatUiEvent.OnDialogType(DialogType.FinishedTasks(FinishedTasksState.Failed)))
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun validate(): Boolean {
        setState { copy(message = message.copy(shouldValidate = true)) }
        return when (currentState.message.copy(shouldValidate = true).validate()) {
            is ValidationStatus.Invalid -> false
            is ValidationStatus.Valid -> true
        }
    }

    private fun translate(text: String) {
        viewModelScope.launch {
            translateUseCase(text).collect {
                it.onSuccess {
                    if (currentState.dialogType is DialogType.SelectText)
                        setState {
                            copy(
                                dialogType = (currentState.dialogType as DialogType.SelectText).copy(translation = it)
                            )
                        }
                }.onFailure {
                    setUiMessage(UiMessage(intValue = Res.string.error_translation))
                }
            }
        }
    }

    private fun createTranslation(translation: Translation) {
        setState { copy(dialogType = DialogType.None) }
        viewModelScope.launch {
            createTranslateUseCase(translation = translation).collect {
                it.onSuccess {
                    setUiMessage(
                        UiMessage(
                            messageType = MessageType.Device,
                            status = MessageStatus.Success,
                            intValue = Res.string.translations_created
                        )
                    )
                }.onFailure { apiError ->
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    fun formatSpeechText(input: String): String {
        return input
            .split(Regex("(?<=[.!?])\\s+"))
            .map { sentence ->
                val trimmed = sentence.trim()
                if (trimmed.isEmpty()) return@map ""

                val capitalized = trimmed.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }

                if (!capitalized.endsWith(".") && !capitalized.endsWith("!") && !capitalized.endsWith("?"))
                    "$capitalized."
                else
                    capitalized
            }
            .joinToString(" ")
    }



    /*------------------------- ↓ OnCleared function ↓ -------------------------*/
    override fun onCleared() {
        super.onCleared()
        sttController.shutdown()
    }

}

/**
 * Whether the current compilation target is a browser (JS or WASM).
 *
 * On the web target, the browser `WebSocket` API cannot set custom headers
 * during the handshake, so the JWT must be passed via `?token=` query
 * parameter instead of `Authorization: Bearer *** Native targets
 * (Android, iOS, JVM) send the header.
 */
internal expect fun isWebTarget(): Boolean
