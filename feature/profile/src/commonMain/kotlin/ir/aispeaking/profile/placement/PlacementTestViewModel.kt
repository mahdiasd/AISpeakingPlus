package ir.aispeaking.profile.placement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.model.placement.PlacementSubmission
import ir.aispeaking.domain.usecase.placement.GetPlacementQuestionsUseCase
import ir.aispeaking.domain.usecase.placement.SubmitPlacementTestUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class PlacementTestViewModel(
    private val getPlacementQuestionsUseCase: GetPlacementQuestionsUseCase,
    private val submitPlacementTestUseCase: SubmitPlacementTestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlacementTestUiState())
    val uiState: StateFlow<PlacementTestUiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<PlacementTestUiEffect>()
    val uiEffect: SharedFlow<PlacementTestUiEffect> = _uiEffect.asSharedFlow()

    init {
        loadQuestions()
    }

    fun onEvent(event: PlacementTestUiEvent) {
        when (event) {
            is PlacementTestUiEvent.SelectOption -> selectOption(event.questionId, event.option)
            is PlacementTestUiEvent.PageChanged -> onPageChanged(event.pageIndex)
            is PlacementTestUiEvent.NextQuestion -> nextQuestion()
            is PlacementTestUiEvent.PreviousQuestion -> previousQuestion()
            is PlacementTestUiEvent.SubmitTest -> submitTest()
            is PlacementTestUiEvent.RetryLoad -> loadQuestions()
            is PlacementTestUiEvent.RetakeTest -> retakeTest()
            is PlacementTestUiEvent.ToggleReviewDialog -> toggleReviewDialog()
            is PlacementTestUiEvent.ShowExitConfirm -> _uiState.update { it.copy(showExitConfirmDialog = true) }
            is PlacementTestUiEvent.DismissExitConfirm -> _uiState.update { it.copy(showExitConfirmDialog = false) }
        }
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getPlacementQuestionsUseCase()) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            sessionId = result.data.sessionId,
                            questions = result.data.questions,
                            currentQuestionIndex = 0,
                            selectedAnswers = emptyMap(),
                            evaluationResult = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    val errorMsg = result.appError.getErrorMessage().ifBlank { "خطا در دریافت سوالات آزمون" }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = errorMsg
                        )
                    }
                }
            }
        }
    }

    private fun selectOption(questionId: String, option: String) {
        _uiState.update {
            val updated = it.selectedAnswers.toMutableMap()
            updated[questionId] = option
            it.copy(selectedAnswers = updated)
        }
    }

    private fun onPageChanged(pageIndex: Int) {
        _uiState.update { it.copy(currentQuestionIndex = pageIndex) }
    }

    private fun nextQuestion() {
        val current = _uiState.value.currentQuestionIndex
        val total = _uiState.value.totalQuestions
        if (current < total - 1) {
            val next = current + 1
            _uiState.update { it.copy(currentQuestionIndex = next) }
            viewModelScope.launch {
                _uiEffect.emit(PlacementTestUiEffect.ScrollToPage(next))
            }
        }
    }

    private fun previousQuestion() {
        val current = _uiState.value.currentQuestionIndex
        if (current > 0) {
            val prev = current - 1
            _uiState.update { it.copy(currentQuestionIndex = prev) }
            viewModelScope.launch {
                _uiEffect.emit(PlacementTestUiEffect.ScrollToPage(prev))
            }
        }
    }

    private fun submitTest() {
        val state = _uiState.value
        if (state.sessionId.isBlank() || state.isEvaluating) return

        viewModelScope.launch {
            _uiState.update { it.copy(isEvaluating = true, errorMessage = null) }
            val submission = PlacementSubmission(
                sessionId = state.sessionId,
                answers = state.selectedAnswers
            )
            when (val result = submitPlacementTestUseCase(submission)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isEvaluating = false,
                            evaluationResult = result.data
                        )
                    }
                    _uiEffect.emit(PlacementTestUiEffect.Finished(result.data.assignedLevel.name))
                }
                is DataResult.Failure -> {
                    val errorMsg = result.appError.getErrorMessage().ifBlank { "خطا در ارزیابی و تصحیح آزمون" }
                    _uiState.update {
                        it.copy(
                            isEvaluating = false,
                            errorMessage = errorMsg
                        )
                    }
                }
            }
        }
    }

    private fun retakeTest() {
        _uiState.update {
            it.copy(
                evaluationResult = null,
                showReviewDialog = false,
                selectedAnswers = emptyMap()
            )
        }
        loadQuestions()
    }

    private fun toggleReviewDialog() {
        _uiState.update { it.copy(showReviewDialog = !it.showReviewDialog) }
    }
}
