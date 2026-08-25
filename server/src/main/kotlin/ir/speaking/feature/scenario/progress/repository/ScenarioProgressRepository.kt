package ir.speaking.feature.scenario.progress.repository

import ir.speaking.core.response.PagedList
import ir.speaking.feature.scenario.progress.dto.response.ScenarioProgressResponse
import ir.speaking.feature.scenario.progress.model.ScenarioProgress
import java.util.*

interface ScenarioProgressRepository {
    suspend fun getProgressById(id: UUID): ScenarioProgress?
    suspend fun getProgressByUser(
        userId: UUID,
        page: Int,
        pageSize: Int
    ): PagedList<ScenarioProgressResponse>

    suspend fun createProgress(progress: ScenarioProgress): ScenarioProgress
    suspend fun updateProgress(progress: ScenarioProgress): ScenarioProgress?
    suspend fun getProgressByUserAndScenario(userId: UUID, scenarioId: UUID): ScenarioProgress?

    suspend fun countByUser(userId: UUID): Long
}