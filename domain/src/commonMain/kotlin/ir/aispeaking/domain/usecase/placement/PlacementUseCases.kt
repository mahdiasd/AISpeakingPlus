package ir.aispeaking.domain.usecase.placement

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.domain.model.placement.PlacementQuestionsResponse
import ir.aispeaking.domain.model.placement.PlacementSubmission
import ir.aispeaking.domain.repository.placement.PlacementTestRepository
import org.koin.core.annotation.Factory

@Factory
class GetPlacementQuestionsUseCase(
    private val repository: PlacementTestRepository
) {
    suspend operator fun invoke(): DataResult<PlacementQuestionsResponse> {
        return repository.getPlacementQuestions()
    }
}

@Factory
class SubmitPlacementTestUseCase(
    private val repository: PlacementTestRepository
) {
    suspend operator fun invoke(submission: PlacementSubmission): DataResult<PlacementEvaluationResult> {
        return repository.submitPlacementTest(submission)
    }
}
