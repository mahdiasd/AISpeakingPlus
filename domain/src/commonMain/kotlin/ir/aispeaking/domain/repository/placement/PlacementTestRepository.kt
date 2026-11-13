package ir.aispeaking.domain.repository.placement

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.domain.model.placement.PlacementQuestionsResponse
import ir.aispeaking.domain.model.placement.PlacementSubmission

interface PlacementTestRepository {
    suspend fun getPlacementQuestions(): DataResult<PlacementQuestionsResponse>
    suspend fun submitPlacementTest(submission: PlacementSubmission): DataResult<PlacementEvaluationResult>
}
