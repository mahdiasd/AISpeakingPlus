package ir.aispeaking.data.repository.placement

import ir.aispeaking.data.mapper.placement.toDomain
import ir.aispeaking.data.mapper.placement.toDto
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.domain.model.placement.PlacementQuestionsResponse
import ir.aispeaking.domain.model.placement.PlacementSubmission
import ir.aispeaking.domain.repository.placement.PlacementTestRepository
import ir.aispeaking.network.api.placement.PlacementApi
import ir.aispeaking.storage.preferences.user.UserPreferences
import org.koin.core.annotation.Single

@Single
class PlacementTestRepositoryImpl(
    private val placementApi: PlacementApi,
    private val userPreferences: UserPreferences
) : PlacementTestRepository {

    override suspend fun getPlacementQuestions(): DataResult<PlacementQuestionsResponse> {
        val result = safeCall { placementApi.getQuestions() }
        return when (result) {
            is DataResult.Success -> DataResult.Success(result.data.toDomain())
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun submitPlacementTest(submission: PlacementSubmission): DataResult<PlacementEvaluationResult> {
        val result = safeCall { placementApi.evaluate(submission.toDto()) }
        return when (result) {
            is DataResult.Success -> {
                val domainResult = result.data.toDomain()
                // Update local cached user profile languageLevel
                try {
                    val localUser = userPreferences.read()
                    if (localUser != null) {
                        userPreferences.save(localUser.copy(languageLevel = domainResult.assignedLevel.name))
                    }
                } catch (_: Exception) {}
                DataResult.Success(domainResult)
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }
}
