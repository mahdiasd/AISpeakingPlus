@file:OptIn(kotlin.time.ExperimentalTime::class)

package ir.aispeaking.sharedui.ui.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.domain.usecase.stage.GetStageDetailUseCase
import ir.aispeaking.domain.usecase.stage.RequestStageHintUseCase
import ir.aispeaking.domain.usecase.stage.SendStageChatMessageUseCase
import ir.aispeaking.domain.usecase.stage.SubmitStageEvaluationUseCase
import ir.aispeaking.sharedui.ui.stage.audio.StageAudioController
import ir.aispeaking.sharedui.ui.stage.component.ChatInputMode
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
    val evaluationSession: EvaluationSession? = null,
    val earnedStars: Int = 0,
    val earnedScore: Int = 0,
    val currentlyPlayingUid: String? = null
)

@Factory
class ChatViewModel(
    private val getStageDetailUseCase: GetStageDetailUseCase,
    private val sendStageChatMessageUseCase: SendStageChatMessageUseCase,
    private val requestStageHintUseCase: RequestStageHintUseCase,
    private val submitStageEvaluationUseCase: SubmitStageEvaluationUseCase,
    private val stageRepository: StageRepository,
    private val audioController: StageAudioController
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentStageId: String = ""

    fun initStage(stageId: String, tier: AccessTier = AccessTier.GUEST) {
        if (currentStageId == stageId && _uiState.value.stage != null) return
        currentStageId = stageId

        viewModelScope.launch {
            when (val result = getStageDetailUseCase(stageId, tier)) {
                is DataResult.Success -> {
                    val stage = result.data
                    _uiState.update {
                        it.copy(
                            stage = stage,
                            chats = persistentListOf(),
                            turnsCount = 0,
                            hintsUsedCount = 0,
                            showEvaluationDialog = false,
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
                    // Stage detail fetch error
                }
            }
        }
    }

    private fun requestInitialAiGreeting(stageId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isModelSpeaking = true) }

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
        val content = _uiState.value.messageText.trim()
        if (content.isBlank()) return

        if (_uiState.value.isRecording) {
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
                chats = (it.chats + userChat + Chat.WaitingForAi).toImmutableList(),
                messageText = "",
                turnsCount = updatedTurns,
                isModelSpeaking = true
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

                    // Update user chat with grammar check result
                    val answeredUserChat = userChat.copy(
                        status = ChatStatus.Answered(grammar = turn.grammarFeedbackFa)
                    )

                    _uiState.update { state ->
                        val updated = state.chats.map {
                            if (it.uid == userUid) answeredUserChat else it
                        }
                        state.copy(chats = updated.toImmutableList())
                    }

                    val aiUid = "ai_${Clock.System.now().toEpochMilliseconds()}"
                    streamAiResponse(
                        aiUid = aiUid,
                        fullMessage = turn.message,
                        translatedMessage = turn.translatedMessage,
                        audioUrl = turn.audioUrl
                    )
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
        audioUrl: String?
    ) {
        val hasAudio = !audioUrl.isNullOrBlank()
        val initialAiChat = Chat.Ai(
            uid = aiUid,
            message = "",
            translatedMessage = translatedMessage,
            voiceState = if (hasAudio) AiVoiceState.Playing else AiVoiceState.Stopped,
            audioUrl = audioUrl
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
        _uiState.update { it.copy(messageText = lastUserChat.message) }
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
                    val newHintsCount = _uiState.value.hintsUsedCount + 1
                    _uiState.update {
                        it.copy(
                            isRequestingHint = false,
                            hintsUsedCount = newHintsCount,
                            currentHintSuggestion = "Could you please help me with this?",
                            currentHintExplanation = "آیا می‌توانید در این مورد به من کمک کنید؟"
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
        viewModelScope.launch {
            val dialogue = _uiState.value.chats.mapNotNull {
                when (it) {
                    is Chat.User -> "User" to it.message
                    is Chat.Ai -> "Model" to it.message
                    else -> null
                }
            }
            val hintsCount = _uiState.value.hintsUsedCount
            val turnsCount = _uiState.value.turnsCount

            when (val result = submitStageEvaluationUseCase(stageId, hintsCount, turnsCount, dialogue)) {
                is DataResult.Success -> {
                    val session = result.data
                    saveGuestProgress(stageId, session.calculatedStars, session.score)
                    _uiState.update {
                        it.copy(
                            evaluationSession = session,
                            showEvaluationDialog = true
                        )
                    }
                }
                is DataResult.Failure -> {
                    val penalties = hintsCount
                    val stars = when (penalties) {
                        0 -> 3
                        1 -> 2
                        2 -> 1
                        else -> 0
                    }
                    val session = EvaluationSession(
                        stageId = stageId,
                        hintsUsedCount = hintsCount,
                        grammarErrorsCount = 0,
                        objectiveCompleted = true,
                        grammarErrors = emptyList(),
                        calculatedStars = stars,
                        score = if (stars == 3) 100 else if (stars == 2) 85 else if (stars == 1) 70 else 40,
                        feedbackFa = "مکالمه به پایان رسید و پیشرفت شما ثبت شد."
                    )
                    saveGuestProgress(stageId, session.calculatedStars, session.score)
                    _uiState.update {
                        it.copy(
                            evaluationSession = session,
                            showEvaluationDialog = true
                        )
                    }
                }
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

    fun dismissEvaluationDialog() {
        _uiState.update { it.copy(showEvaluationDialog = false) }
    }

    override fun onCleared() {
        super.onCleared()
        audioController.stopVoice()
        audioController.stopRecording()
    }
}
