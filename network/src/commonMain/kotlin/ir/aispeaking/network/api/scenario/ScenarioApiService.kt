package ir.aispeaking.network.api.scenario

import ir.aispeaking.network.dto.scenario.ScenarioDetailResponse
import ir.aispeaking.network.dto.scenario.ScenarioSummaryResponse
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.network.model.NetworkResponse


interface ScenarioApiService {
    suspend fun getScenario(id: String): NetworkResponse<ScenarioDetailResponse>

    suspend fun getScenarios(
        searchText: String?,
        categoryId: String?,
        page: Int,
        pageSize: Int?
    ): NetworkResponse<List<ScenarioSummaryResponse>>

    suspend fun getChallengeDetail(challengeId: String): NetworkResponse<ScenarioDetailResponse>

    suspend fun createScenarioProgress(scenarioId: String, score: Int): NetworkResponse<UserResponse>
}