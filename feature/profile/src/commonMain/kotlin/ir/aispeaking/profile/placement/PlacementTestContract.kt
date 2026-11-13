package ir.aispeaking.profile.placement

import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.domain.model.placement.PlacementQuestion

data class PlacementTestUiState(
    val isLoading: Boolean = true,
    val isEvaluating: Boolean = false,
    val errorMessage: String? = null,
    val sessionId: String = "",
    val questions: List<PlacementQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<String, String> = emptyMap(), // questionId -> selectedOption
    val evaluationResult: PlacementEvaluationResult? = null,
    val showReviewDialog: Boolean = false,
    val showExitConfirmDialog: Boolean = false
) {
    val totalQuestions: Int get() = questions.size
    val currentQuestion: PlacementQuestion? get() = questions.getOrNull(currentQuestionIndex)
    val hasAnsweredCurrent: Boolean get() = currentQuestion?.let { selectedAnswers.containsKey(it.id) } == true
    val isLastQuestion: Boolean get() = currentQuestionIndex == totalQuestions - 1 && totalQuestions > 0
    val answeredCount: Int get() = selectedAnswers.size
    val progressFraction: Float get() = if (totalQuestions > 0) (currentQuestionIndex + 1).toFloat() / totalQuestions else 0f
}

sealed interface PlacementTestUiEvent {
    data class SelectOption(val questionId: String, val option: String) : PlacementTestUiEvent
    data class PageChanged(val pageIndex: Int) : PlacementTestUiEvent
    data object NextQuestion : PlacementTestUiEvent
    data object PreviousQuestion : PlacementTestUiEvent
    data object SubmitTest : PlacementTestUiEvent
    data object RetryLoad : PlacementTestUiEvent
    data object RetakeTest : PlacementTestUiEvent
    data object ToggleReviewDialog : PlacementTestUiEvent
    data object ShowExitConfirm : PlacementTestUiEvent
    data object DismissExitConfirm : PlacementTestUiEvent
}

sealed interface PlacementTestUiEffect {
    data class ScrollToPage(val pageIndex: Int) : PlacementTestUiEffect
    data class Finished(val assignedLevel: String) : PlacementTestUiEffect
}
