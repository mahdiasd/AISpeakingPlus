package ir.aispeaking.domain.repository.scenario

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.domain.model.scenario.ScenarioDetail
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface ScenarioRepository {

    suspend fun getScenario(id: String): Flow<DataResult<ScenarioDetail>>

    suspend fun getScenarios(
        searchText: String?,
        categoryId: String?,
        page: Int,
        pageSize: Int? = null
    ): Flow<DataResult<Paging<ScenarioSummary>>>

    suspend fun getSharedPrefScenario(): Scenario?
    suspend fun saveScenarioToSharedPref(scenario: Scenario)

    suspend fun getChallengeDetail(challengeId: String): Flow<DataResult<ScenarioDetail>>

    suspend fun createScenarioProgress(scenarioId: String, score: Int): Flow<DataResult<User>>
}