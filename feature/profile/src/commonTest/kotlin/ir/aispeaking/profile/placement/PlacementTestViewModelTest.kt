package ir.aispeaking.profile.placement

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.domain.model.placement.PlacementQuestion
import ir.aispeaking.domain.model.placement.PlacementQuestionsResponse
import ir.aispeaking.domain.model.placement.PlacementSubmission
import ir.aispeaking.domain.model.placement.QuestionReviewItem
import ir.aispeaking.domain.repository.placement.PlacementTestRepository
import ir.aispeaking.domain.usecase.placement.GetPlacementQuestionsUseCase
import ir.aispeaking.domain.usecase.placement.SubmitPlacementTestUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlacementTestViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private class FakePlacementTestRepository : PlacementTestRepository {
        val sampleQuestions = (1..20).map { i ->
            PlacementQuestion(
                id = "q_$i",
                questionText = "Question $i",
                options = listOf("Opt A", "Opt B", "Opt C", "Opt D"),
                level = LanguageLevel.A1,
                category = "grammar"
            )
        }

        override suspend fun getPlacementQuestions(): DataResult<PlacementQuestionsResponse> {
            return DataResult.Success(
                PlacementQuestionsResponse(
                    sessionId = "test_sess_123",
                    totalQuestions = sampleQuestions.size,
                    questions = sampleQuestions
                )
            )
        }

        override suspend fun submitPlacementTest(submission: PlacementSubmission): DataResult<PlacementEvaluationResult> {
            return DataResult.Success(
                PlacementEvaluationResult(
                    sessionId = submission.sessionId,
                    assignedLevel = LanguageLevel.B2,
                    scorePercent = 85,
                    correctCount = 17,
                    wrongCount = 3,
                    totalCount = 20,
                    levelTitleFa = "متوسط رو به بالا",
                    descriptionFa = "تسلط خوب",
                    reviewItems = emptyList()
                )
            )
        }
    }

    private lateinit var repository: FakePlacementTestRepository
    private lateinit var getQuestionsUseCase: GetPlacementQuestionsUseCase
    private lateinit var submitUseCase: SubmitPlacementTestUseCase

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakePlacementTestRepository()
        getQuestionsUseCase = GetPlacementQuestionsUseCase(repository)
        submitUseCase = SubmitPlacementTestUseCase(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadQuestionsInitialState() = runTest {
        val viewModel = PlacementTestViewModel(getQuestionsUseCase, submitUseCase)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(20, state.totalQuestions)
        assertEquals("test_sess_123", state.sessionId)
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun testSelectOptionAndPageChange() = runTest {
        val viewModel = PlacementTestViewModel(getQuestionsUseCase, submitUseCase)
        advanceUntilIdle()

        viewModel.onEvent(PlacementTestUiEvent.SelectOption("q_1", "Opt A"))
        assertEquals("Opt A", viewModel.uiState.value.selectedAnswers["q_1"])
        assertEquals(1, viewModel.uiState.value.answeredCount)

        viewModel.onEvent(PlacementTestUiEvent.PageChanged(16)) // Question 17 (0-based 16)
        assertEquals(16, viewModel.uiState.value.currentQuestionIndex)
        assertEquals(17, viewModel.uiState.value.currentQuestion?.id?.removePrefix("q_")?.toIntOrNull())

        viewModel.onEvent(PlacementTestUiEvent.SelectOption("q_17", "Opt C"))
        assertEquals("Opt C", viewModel.uiState.value.selectedAnswers["q_17"])
        assertEquals(2, viewModel.uiState.value.answeredCount)
    }

    @Test
    fun testSubmitTestSuccess() = runTest {
        val viewModel = PlacementTestViewModel(getQuestionsUseCase, submitUseCase)
        advanceUntilIdle()

        viewModel.onEvent(PlacementTestUiEvent.SelectOption("q_1", "Opt A"))
        viewModel.onEvent(PlacementTestUiEvent.SubmitTest)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEvaluating)
        assertNotNull(state.evaluationResult)
        assertEquals(LanguageLevel.B2, state.evaluationResult.assignedLevel)
        assertEquals(85, state.evaluationResult.scorePercent)
    }
}
