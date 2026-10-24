@file:OptIn(kotlin.time.ExperimentalTime::class)

package ir.aispeaking.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.domain.model.chat.StageChatStreamEvent
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.GrammarErrorDetail
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.stage.GetStageDetailUseCase
import ir.aispeaking.domain.usecase.stage.RequestStageHintUseCase
import ir.aispeaking.domain.usecase.stage.SubmitStageEvaluationUseCase
import ir.aispeaking.domain.usecase.stt.CloseSttSessionUseCase
import ir.aispeaking.domain.usecase.stt.SendAudioChunkUseCase
import ir.aispeaking.domain.usecase.stt.StreamSpeechToTextUseCase
import ir.aispeaking.domain.usecase.user.GetUserProfileUseCase
import ir.aispeaking.chat.audio.StageAudioController
import ir.aispeaking.chat.component.ChatInputMode
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.analytics.AnalyticsEvents
import ir.aispeaking.utils.analytics.logStageOpened
import ir.aispeaking.utils.analytics.logStageCompleted
import ir.aispeaking.utils.crashlytics.CrashReporter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.koin.core.annotation.Factory

data class ChatUiState(
    val stage: Stage? = null,
    val userLevel: String = "A1",
    val stageLoadError: String? = null,
    val chats: ImmutableList<Chat> = persistentListOf(),
    val inputMode: ChatInputMode = ChatInputMode.VOICE,
    val messageText: String = "",
    val isRecording: Boolean = false,
    val isModelSpeaking: Boolean = false,
    val isRequestingHint: Boolean = false,
    val hintsUsedCount: Int = 0,
    val turnsCount: Int = 0,
    val currentHintSuggestion: String? = null,
    val currentHintExplanation: String? = null,
    val hintError: String? = null,
    val sttError: String? = null,
    val showEvaluationDialog: Boolean = false,
    val isObjectiveCompleted: Boolean = false,
    val showFinishConfirmDialog: Boolean = false,
    val isSubmittingEvaluation: Boolean = false,
    val evaluationSession: EvaluationSession? = null,
    val earnedStars: Int = 0,
    val earnedScore: Int = 0,
    val currentlyPlayingUid: String? = null,
    val showMicPermissionDialog: Boolean = false,
    val isMicPermissionGranted: Boolean = false,
    val isMicPermissionPermanentlyDenied: Boolean = false
)

@Factory
class ChatViewModel(
    private val getStageDetailUseCase: GetStageDetailUseCase,
    private val getCurrentAccessTierUseCase: GetCurrentAccessTierUseCase,
    private val requestStageHintUseCase: RequestStageHintUseCase,
    private val submitStageEvaluationUseCase: SubmitStageEvaluationUseCase,
    private val stageRepository: StageRepository,
    private val audioController: StageAudioController,
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val streamSpeechToTextUseCase: StreamSpeechToTextUseCase,
    private val sendAudioChunkUseCase: SendAudioChunkUseCase,
    private val closeSttSessionUseCase: CloseSttSessionUseCase,
    private val analyticsTracker: AnalyticsTracker,
    private val crashReporter: CrashReporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentStageId: String = ""
    private var currentTier: AccessTier = AccessTier.GUEST
    private var sttStreamJob: Job? = null
    private var audioChunkChannel: Channel<ByteArray>? = null
    private var audioSenderJob: Job? = null

    fun initStage(stageId: String, tier: AccessTier? = null) {
        if (currentStageId == stageId && _uiState.value.stage != null) return
        currentStageId = stageId
        _uiState.update { it.copy(stageLoadError = null) }

        viewModelScope.launch {
            val resolvedTier = tier ?: getCurrentAccessTierUseCase()
            currentTier = resolvedTier
            val profileResult = getUserProfileUseCase()
            val userLevel = when (profileResult) {
                is DataResult.Success -> profileResult.data.languageLevel.takeIf { it.isNotBlank() } ?: "A1"
                else -> "A1"
            }

            when (val result = getStageDetailUseCase(stageId, resolvedTier)) {
                is DataResult.Success -> {
                    val stage = result.data
                    analyticsTracker.logStageOpened(
                        stageId = stage.id,
                        stageTitle = stage.title,
                        stageOrder = stage.orderIndex,
                        userLevel = userLevel,
                        accessTier = resolvedTier.name
                    )
                    crashReporter.setCustomKey("current_stage_id", stage.id)
                    crashReporter.setCustomKey("current_stage_title", stage.title)
                    crashReporter.log("Stage loaded: ${stage.id} - ${stage.title}")

                    _uiState.update {
                        it.copy(
                            stage = stage,
                            userLevel = userLevel,
                            stageLoadError = null,
                            chats = persistentListOf(),
                            turnsCount = 0,
                            hintsUsedCount = 0,
                            showEvaluationDialog = false,
                            isObjectiveCompleted = false,
                            showFinishConfirmDialog = false,
                            isSubmittingEvaluation = false,
                            evaluationSession = null,
                            currentHintSuggestion = null,
                            currentHintExplanation = null
                        )
                    }

                    // Requirement 1: If conversation is started by AI, request initial message from server
                    if (stage.initialSpeaker == "Model") {
                        requestInitialAiGreeting(stageId)
                    }
                    checkMicrophonePermission()
                }
                is DataResult.Failure -> {
                    analyticsTracker.logEvent(
                        "stage_load_failed",
                        mapOf(
                            AnalyticsEvents.PARAM_STAGE_ID to stageId,
                            AnalyticsEvents.PARAM_ERROR to result.appError.getErrorMessage()
                        )
                    )
                    crashReporter.log("Stage load failed for $stageId")
                    _uiState.update {
                        it.copy(
                            stage = null,
                            stageLoadError = "امکان بارگذاری اطلاعات این مرحله وجود ندارد. لطفاً دسترسی یا اتصال اینترنت خود را بررسی کنید."
                        )
                    }
                }
            }
        }
    }

    fun retryLoadStage() {
        val id = currentStageId
        if (id.isNotBlank()) {
            currentStageId = ""
            initStage(id)
        }
    }

    private fun requestInitialAiGreeting(stageId: String) {
        viewModelScope.launch {
            val level = _uiState.value.userLevel
            val aiUid = "ai_init_${Clock.System.now().toEpochMilliseconds()}"
            var accumulatedText = ""
            var isDone = false
            var starterError: String? = null

            val initialAiChat = Chat.Ai(
                uid = aiUid,
                message = "",
                voiceState = AiVoiceState.Stopped
            )

            _uiState.update {
                it.copy(
                    chats = kotlinx.collections.immutable.persistentListOf(initialAiChat),
                    isModelSpeaking = true
                )
            }

            try {
                stageRepository.streamStageChatMessage(
                    stageId = stageId,
                    userMessage = null,
                    history = emptyList(),
                    userLevel = level
                ).collect { event ->
                    when (event) {
                        is StageChatStreamEvent.Connected -> Unit
                        is StageChatStreamEvent.Chunk -> {
                            accumulatedText += event.delta
                            _uiState.update { state ->
                                val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
                                val existingIndex = withoutWaiting.indexOfFirst { it.uid == aiUid }
                                val updatedChats = if (existingIndex != -1) {
                                    withoutWaiting.map { chat ->
                                        if (chat.uid == aiUid && chat is Chat.Ai) chat.copy(message = accumulatedText) else chat
                                    }
                                } else {
                                    withoutWaiting + Chat.Ai(
                                        uid = aiUid,
                                        message = accumulatedText,
                                        voiceState = AiVoiceState.Stopped
                                    )
                                }
                                state.copy(
                                    chats = updatedChats.toImmutableList(),
                                    isModelSpeaking = true
                                )
                            }
                        }
                        is StageChatStreamEvent.Done -> {
                            isDone = true
                            val d = event.payload
                            val hasAudio = !d.audioUrl.isNullOrBlank()
                            val finalAiChat = Chat.Ai(
                                uid = aiUid,
                                message = if (d.message.isNotBlank()) d.message else accumulatedText,
                                translatedMessage = d.translatedMessage,
                                voiceState = if (hasAudio) AiVoiceState.Playing else AiVoiceState.Stopped,
                                audioUrl = d.audioUrl,
                                objectiveCompleted = d.objectiveCompleted,
                                finishTaskIndexes = d.finishTaskIndexes.toImmutableList()
                            )

                            _uiState.update { state ->
                                val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
                                val existingIndex = withoutWaiting.indexOfFirst { it.uid == aiUid }
                                val updatedChats = if (existingIndex != -1) {
                                    withoutWaiting.map { if (it.uid == aiUid) finalAiChat else it }
                                } else {
                                    withoutWaiting + finalAiChat
                                }
                                state.copy(
                                    chats = updatedChats.toImmutableList(),
                                    isModelSpeaking = false,
                                    currentlyPlayingUid = if (hasAudio) aiUid else null
                                )
                            }

                            if (hasAudio) {
                                audioController.playVoice(d.audioUrl) {
                                    onVoicePlaybackEnded(aiUid)
                                }
                            }
                        }
                        is StageChatStreamEvent.Error -> {
                            starterError = event.message
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                starterError = e.message
            }

            if (!isDone && accumulatedText.isBlank()) {
                _uiState.update {
                    it.copy(
                        chats = kotlinx.collections.immutable.persistentListOf(),
                        isModelSpeaking = false,
                        stageLoadError = starterError ?: "خطا در شروع مکالمه با هوش مصنوعی. لطفاً دوباره تلاش کنید."
                    )
                }
            }
        }
    }

    fun playAiVoice(uid: String) {
        val targetChat = _uiState.value.chats.find { it.uid == uid } as? Chat.Ai ?: return

        if (targetChat.audioUrl.isNullOrBlank()) {
            return
        }

        // Requirement 3: Toggle play / stop
        if (targetChat.voiceState == AiVoiceState.Playing) {
            stopAiVoice(uid)
            return
        }

        // Set target chat to Playing, all others to Stopped
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it is Chat.Ai) {
                    if (it.uid == uid) it.copy(voiceState = AiVoiceState.Playing)
                    else it.copy(voiceState = AiVoiceState.Stopped)
                } else it
            }
            state.copy(chats = updated.toImmutableList(), currentlyPlayingUid = uid)
        }

        audioController.playVoice(targetChat.audioUrl) {
            onVoicePlaybackEnded(uid)
        }
    }

    fun stopAiVoice(uid: String) {
        audioController.stopVoice()
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it is Chat.Ai && it.uid == uid) it.copy(voiceState = AiVoiceState.Stopped)
                else it
            }
            state.copy(chats = updated.toImmutableList(), currentlyPlayingUid = null)
        }
    }

    private fun onVoicePlaybackEnded(uid: String) {
        _uiState.update { state ->
            val updated = state.chats.map {
                if (it is Chat.Ai && it.uid == uid) it.copy(voiceState = AiVoiceState.Stopped)
                else it
            }
            state.copy(
                chats = updated.toImmutableList(),
                currentlyPlayingUid = if (state.currentlyPlayingUid == uid) null else state.currentlyPlayingUid
            )
        }
    }

    fun checkMicrophonePermission() {
        audioController.checkMicrophonePermission { isGranted ->
            _uiState.update {
                it.copy(
                    isMicPermissionGranted = isGranted,
                    showMicPermissionDialog = !isGranted,
                    isMicPermissionPermanentlyDenied = false
                )
            }
        }
    }

    fun requestMicrophonePermission() {
        audioController.requestMicrophonePermission { isGranted ->
            _uiState.update {
                it.copy(
                    isMicPermissionGranted = isGranted,
                    showMicPermissionDialog = !isGranted,
                    isMicPermissionPermanentlyDenied = !isGranted
                )
            }
        }
    }

    fun dismissMicPermissionDialog() {
        _uiState.update { it.copy(showMicPermissionDialog = false) }
    }

    // Requirements 4 & 5: Voice recording, realtime transcription, and 5-second silence auto-pause
    fun toggleRecording(start: Boolean) {
        if (start) {
            if (!_uiState.value.isMicPermissionGranted) {
                audioController.checkMicrophonePermission { isGranted ->
                    if (isGranted) {
                        _uiState.update { it.copy(isMicPermissionGranted = true) }
                        startRecordingInternal()
                    } else {
                        _uiState.update {
                            it.copy(
                                showMicPermissionDialog = true,
                                isMicPermissionPermanentlyDenied = false
                            )
                        }
                    }
                }
                return
            }
            startRecordingInternal()
        } else {
            stopRecordingSession()
            _uiState.update { it.copy(isRecording = false) }
        }
    }

    private fun startRecordingInternal() {
        audioController.stopVoice()
        _uiState.update { it.copy(isRecording = true, sttError = null, hintError = null) }

            audioSenderJob?.cancel()
            audioChunkChannel?.close()
            val chunkChannel = Channel<ByteArray>(capacity = Channel.BUFFERED)
            audioChunkChannel = chunkChannel

            audioSenderJob = viewModelScope.launch {
                for (chunk in chunkChannel) {
                    try {
                        sendAudioChunkUseCase(chunk)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                    }
                }
            }

            audioController.onAudioChunkCaptured = { pcm16Bytes ->
                audioChunkChannel?.trySend(pcm16Bytes)
            }

            sttStreamJob?.cancel()
            sttStreamJob = viewModelScope.launch {
                var sessionFinalizedText = ""
                try {
                    streamSpeechToTextUseCase().collect { event ->
                        when (event) {
                            is SttStreamEvent.Ready -> {
                                audioController.onAudioChunkCaptured = { pcm16Bytes ->
                                    audioChunkChannel?.trySend(pcm16Bytes)
                                }
                            }
                            is SttStreamEvent.PartialTranscript -> {
                                if (event.text.isNotBlank()) {
                                    val combined = if (sessionFinalizedText.isEmpty()) {
                                        event.text
                                    } else {
                                        "$sessionFinalizedText ${event.text}"
                                    }
                                    _uiState.update { it.copy(messageText = combined) }
                                }
                            }
                            is SttStreamEvent.FinalTranscript -> {
                                if (event.text.isNotBlank()) {
                                    sessionFinalizedText = if (sessionFinalizedText.isEmpty()) {
                                        event.text
                                    } else {
                                        "$sessionFinalizedText ${event.text}"
                                    }
                                    _uiState.update { it.copy(messageText = sessionFinalizedText) }
                                }
                            }
                            is SttStreamEvent.Error -> {
                                stopRecordingSession()
                                val errorMsg = event.message.ifBlank {
                                    "خطا در سرویس تشخیص گفتار. لطفاً دوباره تلاش کنید."
                                }
                                _uiState.update {
                                    it.copy(
                                        isRecording = false,
                                        sttError = errorMsg,
                                        hintError = errorMsg
                                    )
                                }
                            }
                            is SttStreamEvent.Disconnected -> {
                                if (_uiState.value.isRecording) {
                                    stopRecordingSession()
                                    val errorMsg = "اتصال وب‌سوکت تشخیص گفتار قطع شد."
                                    _uiState.update {
                                        it.copy(
                                            isRecording = false,
                                            sttError = errorMsg,
                                            hintError = errorMsg
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    stopRecordingSession()
                    val errorMsg = e.message?.takeIf { it.isNotBlank() }
                        ?: "خطا در ارتباط با سرویس تشخیص گفتار."
                    _uiState.update {
                        it.copy(
                            isRecording = false,
                            sttError = errorMsg,
                            hintError = errorMsg
                        )
                    }
                }
            }

            audioController.startRecording(
                onSpeechRecognized = { text ->
                    _uiState.update { it.copy(messageText = text) }
                },
                onSilenceDetected = {
                    // 5-second silence detected -> auto pause & stop recording
                    stopRecordingSession()
                    _uiState.update { it.copy(isRecording = false) }
                }
            )
    }

    private fun stopRecordingSession() {
        audioController.onAudioChunkCaptured = null
        audioController.stopRecording()
        audioChunkChannel?.close()
        audioChunkChannel = null
        audioSenderJob?.cancel()
        audioSenderJob = null
        sttStreamJob?.cancel()
        sttStreamJob = null
        viewModelScope.launch {
            try {
                closeSttSessionUseCase()
            } catch (_: Exception) {}
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    fun setInputMode(mode: ChatInputMode) {
        if (mode == ChatInputMode.TEXT && _uiState.value.isRecording) {
            stopRecordingSession()
            _uiState.update { it.copy(isRecording = false, inputMode = mode) }
        } else {
            _uiState.update { it.copy(inputMode = mode) }
        }
    }

    fun clearMessageText() {
        _uiState.update { it.copy(messageText = "") }
    }

    // Requirement 4 & 7: Send message, grammar evaluation & beautiful correction box
    fun sendMessage() {
        val stateSnapshot = _uiState.value
        if (stateSnapshot.isSubmittingEvaluation || stateSnapshot.isModelSpeaking) return
        val maxTurnsLimit = stateSnapshot.stage?.maxTurns ?: 12
        if (stateSnapshot.turnsCount >= maxTurnsLimit) {
            submitEvaluation()
            return
        }

        val content = stateSnapshot.messageText.trim()
        if (content.isBlank()) return

        analyticsTracker.logEvent(
            AnalyticsEvents.EVENT_CHAT_MESSAGE_SENT,
            mapOf(
                AnalyticsEvents.PARAM_STAGE_ID to currentStageId,
                AnalyticsEvents.PARAM_INPUT_MODE to stateSnapshot.inputMode.name,
                AnalyticsEvents.PARAM_TURNS_COUNT to stateSnapshot.turnsCount
            )
        )

        if (stateSnapshot.isRecording) {
            stopRecordingSession()
            _uiState.update { it.copy(isRecording = false) }
        }

        val userUid = "user_${Clock.System.now().toEpochMilliseconds()}"
        val userChat = Chat.User(
            uid = userUid,
            message = content,
            status = ChatStatus.Sending
        )

        val aiUid = "ai_${Clock.System.now().toEpochMilliseconds()}"
        val initialAiChat = Chat.Ai(
            uid = aiUid,
            message = "",
            voiceState = AiVoiceState.Stopped
        )

        val updatedTurns = _uiState.value.turnsCount + 1

        _uiState.update {
            it.copy(
                chats = (it.chats + userChat + initialAiChat).toImmutableList(),
                messageText = "",
                turnsCount = updatedTurns,
                isModelSpeaking = true
            )
        }

        viewModelScope.launch {
            val history = _uiState.value.chats
                .filter { it.uid != userUid && it.uid != aiUid && it !is Chat.WaitingForAi }
                .mapNotNull {
                    when (it) {
                        is Chat.User -> "User" to it.message
                        is Chat.Ai -> "Model" to it.message
                        else -> null
                    }
                }

            var accumulatedText = ""
            var isDone = false
            var streamError: String? = null

            try {
                stageRepository.streamStageChatMessage(
                    stageId = currentStageId,
                    userMessage = content,
                    history = history,
                    userLevel = _uiState.value.userLevel
                ).collect { event ->
                    when (event) {
                        is StageChatStreamEvent.Connected -> {
                            _uiState.update { state -> state.copy(isModelSpeaking = true) }
                        }
                        is StageChatStreamEvent.Chunk -> {
                            accumulatedText += event.delta
                            _uiState.update { state ->
                                val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
                                val existingIndex = withoutWaiting.indexOfFirst { it.uid == aiUid }
                                val updatedChats = if (existingIndex != -1) {
                                    withoutWaiting.map { chat ->
                                        if (chat.uid == aiUid && chat is Chat.Ai) chat.copy(message = accumulatedText) else chat
                                    }
                                } else {
                                    withoutWaiting + Chat.Ai(
                                        uid = aiUid,
                                        message = accumulatedText,
                                        voiceState = AiVoiceState.Stopped
                                    )
                                }
                                state.copy(
                                    chats = updatedChats.toImmutableList(),
                                    isModelSpeaking = true
                                )
                            }
                        }
                        is StageChatStreamEvent.Done -> {
                            isDone = true
                            val turn = event.payload
                            val answeredUserChat = userChat.copy(
                                status = ChatStatus.Answered(
                                    grammar = turn.grammarFeedbackFa,
                                    correctedSentence = turn.correctedSentence,
                                    hasGrammarError = turn.hasGrammarError || turn.grammarFeedbackFa.isNotBlank()
                                )
                            )

                            val isTurnGoalCompleted = turn.objectiveCompleted
                            val isGoalCompleted = isTurnGoalCompleted || _uiState.value.isObjectiveCompleted
                            val hasAudio = !turn.audioUrl.isNullOrBlank()

                            val finalAiChat = Chat.Ai(
                                uid = aiUid,
                                message = if (turn.message.isNotBlank()) turn.message else accumulatedText,
                                translatedMessage = turn.translatedMessage,
                                voiceState = if (hasAudio) AiVoiceState.Playing else AiVoiceState.Stopped,
                                audioUrl = turn.audioUrl,
                                objectiveCompleted = isTurnGoalCompleted,
                                finishTaskIndexes = turn.finishTaskIndexes.toImmutableList()
                            )

                            _uiState.update { state ->
                                val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
                                val withUser = withoutWaiting.map { if (it.uid == userUid) answeredUserChat else it }
                                val existingIndex = withUser.indexOfFirst { it.uid == aiUid }
                                val updatedChats = if (existingIndex != -1) {
                                    withUser.map { if (it.uid == aiUid) finalAiChat else it }
                                } else {
                                    withUser + finalAiChat
                                }
                                state.copy(
                                    chats = updatedChats.toImmutableList(),
                                    isModelSpeaking = false,
                                    currentlyPlayingUid = if (hasAudio) aiUid else null,
                                    isObjectiveCompleted = isGoalCompleted
                                )
                            }

                            val maxTurns = _uiState.value.stage?.maxTurns ?: 12
                            val shouldAutoComplete = isGoalCompleted || _uiState.value.turnsCount >= maxTurns

                            if (hasAudio) {
                                var hasAutoFinished = false
                                val triggerAutoFinish = {
                                    if (!hasAutoFinished) {
                                        hasAutoFinished = true
                                        viewModelScope.launch {
                                            delay(500)
                                            submitEvaluation()
                                        }
                                    }
                                }

                                audioController.playVoice(turn.audioUrl) {
                                    onVoicePlaybackEnded(aiUid)
                                    if (shouldAutoComplete) {
                                        triggerAutoFinish()
                                    }
                                }

                                if (shouldAutoComplete) {
                                    viewModelScope.launch {
                                        delay(25000)
                                        triggerAutoFinish()
                                    }
                                }
                            } else {
                                if (shouldAutoComplete) {
                                    delay(900)
                                    submitEvaluation()
                                }
                            }
                        }
                        is StageChatStreamEvent.Error -> {
                            streamError = event.message
                        }
                    }
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                streamError = e.message ?: "خطا در ارتباط با سرور"
            }

            if (!isDone) {
                val failedUserChat = userChat.copy(status = ChatStatus.Failed)
                _uiState.update { state ->
                    val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi && it.uid != aiUid }
                    val updated = withoutWaiting.map {
                        if (it.uid == userUid) failedUserChat else it
                    }
                    state.copy(
                        chats = updated.toImmutableList(),
                        isModelSpeaking = false,
                        turnsCount = (state.turnsCount - 1).coerceAtLeast(0)
                    )
                }
            }
        }
    }

    fun retrySendMessage() {
        val lastUserChat = _uiState.value.chats.lastOrNull { it is Chat.User } as? Chat.User ?: return
        _uiState.update { state ->
            state.copy(
                chats = state.chats.filter { it.uid != lastUserChat.uid }.toImmutableList(),
                messageText = lastUserChat.message
            )
        }
        sendMessage()
    }

    fun requestHint() {
        val stageId = _uiState.value.stage?.id ?: return
        analyticsTracker.logEvent(
            AnalyticsEvents.EVENT_HINT_REQUESTED,
            mapOf(AnalyticsEvents.PARAM_STAGE_ID to stageId)
        )
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingHint = true, hintError = null) }
            val dialogue = _uiState.value.chats.mapNotNull {
                when (it) {
                    is Chat.User -> "User" to it.message
                    is Chat.Ai -> "Model" to it.message
                    else -> null
                }
            }
            when (val result = requestStageHintUseCase(stageId, dialogue, _uiState.value.userLevel)) {
                is DataResult.Success -> {
                    val newHintsCount = _uiState.value.hintsUsedCount + 1
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            hintsUsedCount = newHintsCount,
                            currentHintSuggestion = result.data.suggestionEn,
                            currentHintExplanation = result.data.explanationFa,
                            hintError = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    // Do not increment hintsUsedCount on network failure
                    val errorMsg = result.appError.getErrorMessage().ifBlank {
                        "خطا در دریافت راهنمایی. لطفاً اتصال اینترنت خود را بررسی کنید."
                    }
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            currentHintSuggestion = null,
                            currentHintExplanation = null,
                            hintError = errorMsg
                        )
                    }
                }
            }
        }
    }

    fun dismissHint() {
        _uiState.update {
            it.copy(
                currentHintSuggestion = null,
                currentHintExplanation = null,
                hintError = null,
                sttError = null
            )
        }
    }

    fun submitEvaluation() {
        audioController.stopVoice()
        val stageId = _uiState.value.stage?.id ?: return
        if (_uiState.value.isSubmittingEvaluation || _uiState.value.showFinishConfirmDialog) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingEvaluation = true) }
            val hasAiCompletionInChatList = _uiState.value.chats
                .filterIsInstance<Chat.Ai>()
                .any { it.objectiveCompleted }
            val isGoalDone = _uiState.value.isObjectiveCompleted || hasAiCompletionInChatList

            val dialogue = _uiState.value.chats.mapNotNull {
                when (it) {
                    is Chat.User -> "User" to it.message
                    is Chat.Ai -> "Model" to it.message
                    else -> null
                }
            }
            val hintsCount = _uiState.value.hintsUsedCount
            val turnsCount = _uiState.value.turnsCount

            val localGrammarErrors = _uiState.value.chats
                .filterIsInstance<Chat.User>()
                .mapNotNull { userChat ->
                    val status = userChat.status as? ChatStatus.Answered
                    if (status != null && (status.hasGrammarError || status.grammar.isNotBlank())) {
                        GrammarErrorDetail(
                            original = userChat.message,
                            correction = status.correctedSentence?.takeIf { it.isNotBlank() } ?: userChat.message,
                            explanationFa = status.grammar
                        )
                    } else null
                }
            val localGrammarErrorsCount = localGrammarErrors.size

            val session = when (val result = submitStageEvaluationUseCase(
                stageId = stageId,
                hintsUsedCount = hintsCount,
                turnsCount = turnsCount,
                transcript = dialogue,
                grammarErrorsCount = localGrammarErrorsCount,
                grammarErrors = localGrammarErrors,
                objectiveCompleted = isGoalDone
            )) {
                is DataResult.Success -> {
                    val serverSession = result.data
                    val finalGrammarErrorsCount = maxOf(serverSession.grammarErrorsCount, localGrammarErrorsCount)
                    val finalGrammarErrors = if (serverSession.grammarErrors.size >= localGrammarErrors.size && serverSession.grammarErrors.isNotEmpty()) {
                        serverSession.grammarErrors
                    } else {
                        localGrammarErrors
                    }

                    if (!isGoalDone || !serverSession.objectiveCompleted) {
                        serverSession.copy(
                            objectiveCompleted = false,
                            grammarErrorsCount = finalGrammarErrorsCount,
                            grammarErrors = finalGrammarErrors,
                            calculatedStars = 0,
                            score = 0
                        )
                    } else {
                        val totalPenalties = hintsCount + finalGrammarErrorsCount
                        val stars = when {
                            totalPenalties == 0 -> 3
                            totalPenalties == 1 -> 2
                            totalPenalties == 2 -> 1
                            else -> 0
                        }
                        val score = when (stars) {
                            3 -> 100
                            2 -> 85
                            1 -> 70
                            else -> 50
                        }
                        serverSession.copy(
                            objectiveCompleted = true,
                            grammarErrorsCount = finalGrammarErrorsCount,
                            grammarErrors = finalGrammarErrors,
                            calculatedStars = stars,
                            score = score
                        )
                    }
                }
                is DataResult.Failure -> {
                    if (!isGoalDone) {
                        EvaluationSession(
                            stageId = stageId,
                            hintsUsedCount = hintsCount,
                            grammarErrorsCount = localGrammarErrorsCount,
                            objectiveCompleted = false,
                            grammarErrors = localGrammarErrors,
                            calculatedStars = 0,
                            score = 0,
                            feedbackFa = "مکالمه پیش از رسیدن به هدف مرحله پایان یافت. برای دریافت ستاره و تکمیل مرحله، مکالمه را ادامه دهید."
                        )
                    } else {
                        val penalties = hintsCount + localGrammarErrorsCount
                        val stars = when (penalties) {
                            0 -> 3
                            1 -> 2
                            2 -> 1
                            else -> 0
                        }
                        EvaluationSession(
                            stageId = stageId,
                            hintsUsedCount = hintsCount,
                            grammarErrorsCount = localGrammarErrorsCount,
                            objectiveCompleted = true,
                            grammarErrors = localGrammarErrors,
                            calculatedStars = stars,
                            score = when (stars) {
                                3 -> 100
                                2 -> 85
                                1 -> 70
                                else -> 50
                            },
                            feedbackFa = "مکالمه به پایان رسید و پیشرفت شما ثبت شد."
                        )
                    }
                }
            }

            if (currentTier == AccessTier.GUEST && session.objectiveCompleted) {
                saveGuestProgress(stageId, session.calculatedStars, session.score)
            }

            analyticsTracker.logStageCompleted(
                stageId = stageId,
                stageTitle = _uiState.value.stage?.title ?: "",
                score = session.score,
                stars = session.calculatedStars,
                turnsCount = turnsCount,
                hintsUsed = hintsCount,
                passed = session.objectiveCompleted
            )
            crashReporter.log("Stage evaluation completed: $stageId, stars: ${session.calculatedStars}, score: ${session.score}")

            _uiState.update {
                it.copy(
                    isSubmittingEvaluation = false,
                    evaluationSession = session,
                    showFinishConfirmDialog = true,
                    earnedStars = session.calculatedStars,
                    earnedScore = session.score
                )
            }
        }
    }

    private fun saveGuestProgress(stageId: String, stars: Int, score: Int) {
        viewModelScope.launch {
            val progress = LocalGuestProgress(
                stageId = stageId,
                stars = stars,
                score = score,
                completedAt = Clock.System.now().toString()
            )
            stageRepository.saveLocalGuestProgress(progress)
            _uiState.update {
                it.copy(
                    earnedStars = stars,
                    earnedScore = score
                )
            }
        }
    }

    fun dismissFinishConfirmDialog() {
        _uiState.update { it.copy(showFinishConfirmDialog = false) }
    }

    fun replayStage() {
        val stageId = _uiState.value.stage?.id ?: return
        _uiState.update {
            it.copy(
                showFinishConfirmDialog = false,
                evaluationSession = null,
                chats = persistentListOf(),
                isObjectiveCompleted = false,
                turnsCount = 0,
                hintsUsedCount = 0,
                earnedStars = 0,
                earnedScore = 0
            )
        }
        currentStageId = ""
        initStage(stageId)
    }

    override fun onCleared() {
        super.onCleared()
        stopRecordingSession()
        audioController.stopVoice()
    }
}
