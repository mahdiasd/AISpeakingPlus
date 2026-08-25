package ir.speaking.feature.scenario.scenario.repository

import ir.speaking.core.response.PagedList
import ir.speaking.feature.scenario.scenario.dto.response.ScenarioSummaryResponse
import ir.speaking.feature.scenario.scenario.model.Scenario
import java.util.*

interface ScenarioRepository {
    suspend fun getScenarioById(id: UUID): Scenario?

    suspend fun getScenarios(
        page: Int,
        pageSize: Int,
        searchText: String?,
        category: UUID?
    ): PagedList<ScenarioSummaryResponse>

    suspend fun createScenario(scenario: Scenario): Scenario
    suspend fun updateScenario(scenario: Scenario): Scenario?
    suspend fun deleteScenario(id: UUID): Boolean

}