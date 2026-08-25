package ir.speaking.feature.scenario.task.repository

import ir.speaking.feature.scenario.task.model.ScenarioTask
import java.util.*

interface ScenarioTaskRepository {
    suspend fun getScenarioTasksByScenarioId(scenarioId: UUID): List<ScenarioTask>

    suspend fun createScenarioTask(scenarioTask: ScenarioTask): ScenarioTask
    suspend fun createScenarioTasks(scenarioTasks: List<ScenarioTask>): List<ScenarioTask>

    suspend fun deleteScenarioTask(id: UUID): Boolean
    suspend fun deleteScenarioTasksByScenarioId(scenarioId: UUID): Boolean
}