@file:OptIn(kotlin.time.ExperimentalTime::class)

package ir.aispeaking.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.GrammarErrorDetail
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.stage.GetStageDetailUseCase
import ir.aispeaking.domain.usecase.stage.RequestStageHintUseCase
import ir.aispeaking.domain.usecase.stage.SendStageChatMessageUseCase
import ir.aispeaking.domain.usecase.stage.SubmitStageEvaluationUseCase
import ir.aispeaking.chat.audio.StageAudioController
import ir.aispeaking.chat.component.ChatInputMode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
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
    val showEvaluationDialog: Boolean = false,
    val isObjectiveCompleted: Boolean = false,
    val showFinishConfirmDialog: Boolean = false,
    val isSubmittingEvaluation: Boolean = false,
    val evaluationSession: EvaluationSession? = null,
    val earnedStars: Int = 0,
    val earnedScore: Int = 0,
    val currentlyPlayingUid: String? = null
)

@Factory
class ChatViewModel(
    private val getStageDetailUseCase: GetStageDetailUseCase,
    private val getCurrentAccessTierUseCase: GetCurrentAccessTierUseCase,
    private val sendStageChatMessageUseCase: SendStageChatMessageUseCase,
    private val requestStageHintUseCase: RequestStageHintUseCase,
    private val submitStageEvaluationUseCase: SubmitStageEvaluationUseCase,
    private val stageRepository: StageRepository,
    private val audioController: StageAudioController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentStageId: String = ""
    private var currentTier: AccessTier = AccessTier.GUEST

    fun initStage(stageId: String, tier: AccessTier? = null) {
        if (currentStageId == stageId && _uiState.value.stage != null) return
        currentStageId = stageId
        _uiState.update { it.copy(stageLoadError = null) }

        viewModelScope.launch {
            val resolvedTier = tier ?: getCurrentAccessTierUseCase()
            currentTier = resolvedTier
            when (val result = getStageDetailUseCase(stageId, resolvedTier)) {
                is DataResult.Success -> {
                    val stage = result.data
                    _uiState.update {
                        it.copy(
                            stage = stage,
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
                }
                is DataResult.Failure -> {
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
            _uiState.update {
                it.copy(
                    chats = kotlinx.collections.immutable.persistentListOf(Chat.WaitingForAi),
                    isModelSpeaking = true
                )
            }

            when (val result = sendStageChatMessageUseCase(stageId, userMessage = null, history = emptyList())) {
                is DataResult.Success -> {
                    val d = result.data
                    val aiUid = "ai_init_${Clock.System.now().toEpochMilliseconds()}"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = d.message,
                        translatedMessage = d.translatedMessage,
                        audioUrl = d.audioUrl
                    )
                }
                is DataResult.Failure -> {
                    // Fallback initial greeting
                    val aiUid = "ai_init_fallback"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = "Good evening! Welcome aboard. Would you like the grilled chicken with rice, or the vegetarian pasta tonight?",
                        translatedMessage = "عصر بخیر! به پرواز خوش آمدید. امشب مرغ گریل شده با برنج میل دارید یا پاستای گیاهی؟",
                        audioUrl = null
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

    // Requirements 4 & 5: Voice recording, realtime transcription, and 5-second silence auto-pause
    fun toggleRecording(start: Boolean) {
        if (start) {
            audioController.stopVoice()
            _uiState.update { it.copy(isRecording = true) }
            audioController.startRecording(
                onSpeechRecognized = { text ->
                    _uiState.update { it.copy(messageText = text) }
                },
                onSilenceDetected = {
                    // 5-second silence detected -> auto pause
                    _uiState.update { it.copy(isRecording = false) }
                }
            )
        } else {
            audioController.stopRecording()
            _uiState.update { it.copy(isRecording = false) }
        }
    }

    fun onMessageTextChanged(newText: String) {
        _uiState.update { it.copy(messageText = newText) }
    }

    fun setInputMode(mode: ChatInputMode) {
        if (mode == ChatInputMode.TEXT && _uiState.value.isRecording) {
            audioController.stopRecording()
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

        if (stateSnapshot.isRecording) {
            audioController.stopRecording()
            _uiState.update { it.copy(isRecording = false) }
        }

        val userUid = "user_${Clock.System.now().toEpochMilliseconds()}"
        val userChat = Chat.User(
            uid = userUid,
            message = content,
            status = ChatStatus.Sending
        )

        val updatedTurns = _uiState.value.turnsCount + 1

        _uiState.update {
            it.copy(
                chats = (it.chats + userChat).toImmutableList(),
                messageText = "",
                turnsCount = updatedTurns,
                isModelSpeaking = false
            )
        }

        viewModelScope.launch {
            val history = _uiState.value.chats
                .filter { it.uid != userUid && it !is Chat.WaitingForAi }
                .mapNotNull {
                    when (it) {
                        is Chat.User -> "User" to it.message
                        is Chat.Ai -> "Model" to it.message
                        else -> null
                    }
                }

            when (val result = sendStageChatMessageUseCase(currentStageId, content, history)) {
                is DataResult.Success -> {
                    val turn = result.data

                    // Update user chat with AI grammar check result, and now show AI waiting/typing indicator
                    val answeredUserChat = userChat.copy(
                        status = ChatStatus.Answered(
                            grammar = turn.grammarFeedbackFa,
                            correctedSentence = turn.correctedSentence,
                            hasGrammarError = turn.hasGrammarError || turn.grammarFeedbackFa.isNotBlank()
                        )
                    )

                    val isTurnGoalCompleted = turn.objectiveCompleted || turn.finishTaskIndexes.isNotEmpty()
                    val isGoalCompleted = isTurnGoalCompleted || _uiState.value.isObjectiveCompleted

                    _uiState.update { state ->
                        val updated = state.chats.map {
                            if (it.uid == userUid) answeredUserChat else it
                        }
                        state.copy(
                            chats = (updated + Chat.WaitingForAi).toImmutableList(),
                            isModelSpeaking = true,
                            isObjectiveCompleted = isGoalCompleted
                        )
                    }

                    delay(700)

                    val aiUid = "ai_${Clock.System.now().toEpochMilliseconds()}"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = turn.message,
                        translatedMessage = turn.translatedMessage,
                        audioUrl = turn.audioUrl,
                        objectiveCompleted = isTurnGoalCompleted,
                        finishTaskIndexes = turn.finishTaskIndexes
                    )

                    val maxTurns = _uiState.value.stage?.maxTurns ?: 12
                    if (isGoalCompleted || _uiState.value.turnsCount >= maxTurns) {
                        delay(900)
                        submitEvaluation()
                    }
                }
                is DataResult.Failure -> {
                    // Mark as failed
                    val failedUserChat = userChat.copy(status = ChatStatus.Failed)
                    _uiState.update { state ->
                        val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
                        val updated = withoutWaiting.map {
                            if (it.uid == userUid) failedUserChat else it
                        }
                        state.copy(
                            chats = updated.toImmutableList(),
                            isModelSpeaking = false
                        )
                    }
                }
            }
        }
    }

    private suspend fun streamAiResponse(
        aiUid: String,
        fullMessage: String,
        translatedMessage: String?,
        audioUrl: String?,
        objectiveCompleted: Boolean = false,
        finishTaskIndexes: List<Int> = emptyList()
    ) {
        val hasAudio = !audioUrl.isNullOrBlank()
        val initialAiChat = Chat.Ai(
            uid = aiUid,
            message = "",
            translatedMessage = translatedMessage,
            voiceState = if (hasAudio) AiVoiceState.Playing else AiVoiceState.Stopped,
            audioUrl = audioUrl,
            objectiveCompleted = objectiveCompleted,
            finishTaskIndexes = finishTaskIndexes.toImmutableList()
        )

        _uiState.update { state ->
            val withoutWaiting = state.chats.filter { it !is Chat.WaitingForAi }
            state.copy(
                chats = (withoutWaiting + initialAiChat).toImmutableList(),
                isModelSpeaking = false,
                currentlyPlayingUid = if (hasAudio) aiUid else null
            )
        }

        if (hasAudio) {
            audioController.playVoice(audioUrl) {
                onVoicePlaybackEnded(aiUid)
            }
        }

        // Progressive word-by-word streaming effect
        val words = fullMessage.split(" ")
        var accumulated = ""
        for (i in words.indices) {
            accumulated += (if (i == 0) "" else " ") + words[i]
            _uiState.update { state ->
                state.copy(
                    chats = state.chats.map { chat ->
                        if (chat.uid == aiUid && chat is Chat.Ai) {
                            chat.copy(message = accumulated)
                        } else chat
                    }.toImmutableList()
                )
            }
            delay(40)
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
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingHint = true) }
            val dialogue = _uiState.value.chats.mapNotNull {
                when (it) {
                    is Chat.User -> "User" to it.message
                    is Chat.Ai -> "Model" to it.message
                    else -> null
                }
            }
            when (val result = requestStageHintUseCase(stageId, dialogue)) {
                is DataResult.Success -> {
                    val newHintsCount = _uiState.value.hintsUsedCount + 1
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            hintsUsedCount = newHintsCount,
                            currentHintSuggestion = result.data.suggestionEn,
                            currentHintExplanation = result.data.explanationFa
                        )
                    }
                }
                is DataResult.Failure -> {
                    // Do not increment hintsUsedCount on network failure
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            currentHintSuggestion = null,
                            currentHintExplanation = null
                        )
                    }
                }
            }
        }
    }

    fun dismissHint() {
        _uiState.update {
            it.copy(currentHintSuggestion = null, currentHintExplanation = null)
        }
    }

    fun submitEvaluation() {
        val stageId = _uiState.value.stage?.id ?: return
        if (_uiState.value.isSubmittingEvaluation) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmittingEvaluation = true) }
            val hasAiCompletionInChatList = _uiState.value.chats
                .filterIsInstance<Chat.Ai>()
                .any { it.objectiveCompleted || !it.finishTaskIndexes.isNullOrEmpty() }
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
        audioController.stopVoice()
        audioController.stopRecording()
    }
}
