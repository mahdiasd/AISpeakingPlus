package ir.speaking.feature.scenario.task.repository

import ir.speaking.core.exeptions.AppException
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.scenario.task.db.ScenarioTaskDAO
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.scenario.task.db.toModel
import ir.speaking.feature.scenario.task.model.ScenarioTask
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteWhere
import org.koin.core.annotation.Single
import java.util.*

@Single
class ScenarioTaskRepositoryImpl : ScenarioTaskRepository {
    override suspend fun getScenarioTasksByScenarioId(scenarioId: UUID): List<ScenarioTask> =
        suspendTransaction {
            ScenarioTaskDAO.find { ScenarioTaskTable.scenarioId eq scenarioId }.map { it.toModel() }
        }

    override suspend fun createScenarioTask(scenarioTask: ScenarioTask): ScenarioTask =
        suspendTransaction {
            ScenarioTaskDAO.new {
                scenarioId = scenarioTask.scenarioId
                description = scenarioTask.description
                persianDescription = scenarioTask.persianDescription
                createdAt = scenarioTask.createdAt
            }.toModel()
        }

    override suspend fun createScenarioTasks(scenarioTasks: List<ScenarioTask>): List<ScenarioTask> =
        suspendTransaction {
            if (scenarioTasks.isEmpty()) return@suspendTransaction emptyList()

            try {
                // Use batch insert for better performance
                val insertedIds = ScenarioTaskTable.batchInsert(scenarioTasks) { task ->
                    this[ScenarioTaskTable.id] = task.id
                    this[ScenarioTaskTable.scenarioId] = task.scenarioId
                    this[ScenarioTaskTable.description] = task.description
                    this[ScenarioTaskTable.persianDescription] = task.persianDescription
                    this[ScenarioTaskTable.createdAt] = task.createdAt
                }

                // Return the created tasks
                scenarioTasks

            } catch (e: Exception) {
                println("Error creating scenario tasks: ${e.message}")
                e.printStackTrace()
                throw AppException.BadRequest("Failed to create scenario tasks: ${e.message}")
            }
        }

    override suspend fun deleteScenarioTask(id: UUID): Boolean =
        suspendTransaction {
            ScenarioTaskDAO.findById(id)?.let {
                it.delete()
                true
            } ?: false
        }

    override suspend fun deleteScenarioTasksByScenarioId(scenarioId: UUID): Boolean =
        suspendTransaction {
            try {
                ScenarioTaskTable.deleteWhere {
                    ScenarioTaskTable.scenarioId eq scenarioId
                }
                true
            } catch (e: Exception) {
                println("Error deleting scenario tasks for scenario $scenarioId: ${e.message}")
                false
            }
        }
}