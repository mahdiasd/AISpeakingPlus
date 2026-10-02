@file:OptIn(kotlin.time.ExperimentalTime::class)

package ir.aispeaking.sharedui.ui.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.domain.usecase.stage.GetStageDetailUseCase
import ir.aispeaking.domain.usecase.stage.RequestStageHintUseCase
import ir.aispeaking.domain.usecase.stage.SubmitStageEvaluationUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import org.koin.core.annotation.Factory

data class ChatMessage(
    val id: String,
    val role: String, // "Model" or "User"
    val content: String,
    val timestamp: String
)

data class ChatUiState(
    val stage: Stage? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isModelSpeaking: Boolean = false,
    val isRequestingHint: Boolean = false,
    val hintsUsedCount: Int = 0,
    val turnsCount: Int = 0,
    val currentHintSuggestion: String? = null,
    val currentHintExplanation: String? = null,
    val showEvaluationDialog: Boolean = false,
    val evaluationSession: EvaluationSession? = null,
    val earnedStars: Int = 0,
    val earnedScore: Int = 0
)

@Factory
class ChatViewModel(
    private val getStageDetailUseCase: GetStageDetailUseCase,
    private val requestStageHintUseCase: RequestStageHintUseCase,
    private val submitStageEvaluationUseCase: SubmitStageEvaluationUseCase,
    private val stageRepository: StageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun initStage(stageId: String, tier: AccessTier = AccessTier.GUEST) {
        viewModelScope.launch {
            when (val result = getStageDetailUseCase(stageId, tier)) {
                is DataResult.Success -> {
                    val stage = result.data
                    val initialMessages = mutableListOf<ChatMessage>()

                    if (stage.initialSpeaker == "Model") {
                        initialMessages.add(
                            ChatMessage(
                                id = "msg_init",
                                role = "Model",
                                content = "Hello! Welcome to ${stage.title}. How may I help you today?",
                                timestamp = Clock.System.now().toString()
                            )
                        )
                    }

                    _uiState.update {
                        it.copy(
                            stage = stage,
                            messages = initialMessages,
                            turnsCount = 0,
                            hintsUsedCount = 0,
                            showEvaluationDialog = false,
                            evaluationSession = null,
                            currentHintSuggestion = null,
                            currentHintExplanation = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    // Handle error
                }
            }
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val userMessage = ChatMessage(
            id = "msg_${Clock.System.now().toEpochMilliseconds()}",
            role = "User",
            content = content,
            timestamp = Clock.System.now().toString()
        )

        val updatedTurns = _uiState.value.turnsCount + 1

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                turnsCount = updatedTurns,
                isModelSpeaking = true
            )
        }

        // Simulate NPC conversational turn
        viewModelScope.launch {
            val npcReply = generateNpcReply(content, _uiState.value.stage)
            val modelMessage = ChatMessage(
                id = "msg_${Clock.System.now().toEpochMilliseconds()}",
                role = "Model",
                content = npcReply,
                timestamp = Clock.System.now().toString()
            )

            _uiState.update {
                it.copy(
                    messages = it.messages + modelMessage,
                    isModelSpeaking = false
                )
            }
        }
    }

    fun requestHint() {
        val stageId = _uiState.value.stage?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isRequestingHint = true) }
            val dialogue = _uiState.value.messages.map { it.role to it.content }
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
                    // Fallback hint
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
            val dialogue = _uiState.value.messages.map { it.role to it.content }
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
                    // Local fallback evaluation
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
                        feedbackFa = "مکالمه به پایان رسید و پیشرفت ذخیره شد."
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

    fun saveGuestProgress(stageId: String, stars: Int, score: Int) {
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

    private fun generateNpcReply(userText: String, stage: Stage?): String {
        return when (stage?.orderIndex) {
            1 -> {
                if (userText.contains("window", ignoreCase = true)) {
                    "Certainly! I've assigned you seat 14A by the window. Here is your boarding pass. Have a great flight to London!"
                } else {
                    "Sure, I can check in your luggage. Would you prefer an aisle or a window seat?"
                }
            }
            2 -> "Of course! For dinner tonight we have chicken with rice or pasta. What would you like?"
            else -> "Thank you. Let's continue our conversation."
        }
    }
}
