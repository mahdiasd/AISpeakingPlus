package ir.speaking.feature.scenario.scenario.repository

import ir.speaking.core.response.PagedList
import ir.speaking.core.response.PagingMeta
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.scenario.scenario.db.ScenarioDAO
import ir.speaking.feature.scenario.scenario.db.ScenarioTable
import ir.speaking.feature.scenario.scenario.db.toModel
import ir.speaking.feature.scenario.scenario.dto.response.ScenarioSummaryResponse
import ir.speaking.feature.scenario.scenario.model.Scenario
import ir.speaking.feature.scenario.task.db.ScenarioTaskDAO
import ir.speaking.feature.scenario.task.db.ScenarioTaskTable
import ir.speaking.feature.scenario.task.db.toModel
import ir.speaking.feature.scenario.task.model.ScenarioTask
import ir.speaking.feature.scenario.task.repository.ScenarioTaskRepository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.koin.core.annotation.Single
import java.util.*

@Single
class ScenarioRepositoryImpl(
    private val scenarioTaskRepository: ScenarioTaskRepository,
) : ScenarioRepository {
    override suspend fun getScenarioById(id: UUID): Scenario? = suspendTransaction {
        (ScenarioTable leftJoin ScenarioTaskTable)
            .selectAll()
            .andWhere { ScenarioTable.id eq id }
            .let { rows ->
                if (rows.empty()) return@suspendTransaction null

                val firstRow = rows.first()
                val scenario = Scenario(
                    id = firstRow[ScenarioTable.id].value,
                    categoryId = firstRow[ScenarioTable.categoryId],
                    title = firstRow[ScenarioTable.title],
                    description = firstRow[ScenarioTable.description],
                    imageUrl = firstRow[ScenarioTable.imageUrl],
                    aiName = firstRow[ScenarioTable.aiName],
                    aiAvatar = firstRow[ScenarioTable.aiAvatar],
                    points = firstRow[ScenarioTable.points],
                    createdAt = firstRow[ScenarioTable.createdAt],
                    persianTitle = firstRow[ScenarioTable.persianTitle],
                    aiRole = firstRow[ScenarioTable.aiRole],
                    persianDescription = firstRow[ScenarioTable.persianDescription],
                    gender = firstRow[ScenarioTable.gender],
                    starter = firstRow[ScenarioTable.starter],
                    tasks = rows.mapNotNull { row ->
                        if (row.getOrNull(ScenarioTaskTable.id) != null) {
                            ScenarioTask(
                                id = row[ScenarioTaskTable.id].value,
                                scenarioId = row[ScenarioTaskTable.scenarioId],
                                description = row[ScenarioTaskTable.description],
                                persianDescription = row[ScenarioTaskTable.persianDescription],
                                createdAt = row[ScenarioTaskTable.createdAt]
                            )
                        } else null
                    },
                )
                scenario
            }
    }

    override suspend fun getScenarios(
        page: Int,
        pageSize: Int,
        searchText: String?,
        category: UUID?
    ): PagedList<ScenarioSummaryResponse> = suspendTransaction {
        val baseQuery = ScenarioTable
            .select(ScenarioTable.id, ScenarioTable.title, ScenarioTable.imageUrl).apply {
                searchText?.takeIf { it.isNotBlank() }?.let {
                    andWhere { ScenarioTable.title.lowerCase() like "%${it.lowercase().trim()}%" }
                }
                category?.let {
                    andWhere { ScenarioTable.categoryId eq it }
                }
                orderBy(ScenarioTable.createdAt to SortOrder.DESC)
            }

        // Get total count first
        val totalItems = baseQuery.copy().count()

        PagedList(
            items = baseQuery
                .limit(pageSize)
                .offset(((page - 1) * pageSize).toLong())
                .map {
                    ScenarioSummaryResponse(
                        id = it[ScenarioTable.id].value.toString(),
                        title = it[ScenarioTable.title],
                        imageUrl = it[ScenarioTable.imageUrl]
                    )
                },
            pagingMeta = PagingMeta(
                totalPages = ((totalItems + pageSize - 1) / pageSize).toInt(),
                totalItems = totalItems,
                page = page
            )
        )
    }

    override suspend fun createScenario(scenario: Scenario): Scenario = suspendTransaction {
        // Create the scenario first
        val createdScenario = ScenarioDAO.new(scenario.id) {
            categoryId = scenario.categoryId
            title = scenario.title
            description = scenario.description
            persianTitle = scenario.persianTitle
            persianDescription = scenario.persianDescription
            imageUrl = scenario.imageUrl
            aiName = scenario.aiName
            aiAvatar = scenario.aiAvatar
            aiRole = scenario.aiRole
            gender = scenario.gender
            points = scenario.points
            createdAt = scenario.createdAt
            starter = scenario.starter
        }

        // Create tasks
        val createdTasks = scenario.tasks.map { task ->
            ScenarioTaskDAO.new(task.id) {
                scenarioId = createdScenario.id.value
                description = task.description
                persianDescription = task.persianDescription
                createdAt = task.createdAt
            }.toModel()
        }

        // Return scenario with tasks
        createdScenario.toModel().copy(tasks = createdTasks)
    }

    override suspend fun updateScenario(scenario: Scenario): Scenario? {
        // Update scenario in one transaction
        val updatedScenario = suspendTransaction {
            val existingScenario = ScenarioDAO.findById(scenario.id) ?: return@suspendTransaction null

            existingScenario.apply {
                categoryId = scenario.categoryId
                title = scenario.title
                description = scenario.description
                persianTitle = scenario.persianTitle
                persianDescription = scenario.persianDescription
                gender = scenario.gender
                imageUrl = scenario.imageUrl
                aiName = scenario.aiName
                aiRole = scenario.aiRole
                aiAvatar = scenario.aiAvatar
                points = scenario.points
                starter = scenario.starter
            }

            existingScenario.toModel()
        } ?: return null

        // Update tasks in separate calls (outside transaction)
        scenarioTaskRepository.deleteScenarioTasksByScenarioId(scenario.id)
        val createdTasks = if (scenario.tasks.isNotEmpty()) {
            scenarioTaskRepository.createScenarioTasks(scenario.tasks)
        } else {
            emptyList()
        }

        return updatedScenario.copy(tasks = createdTasks)
    }

    override suspend fun deleteScenario(id: UUID): Boolean = suspendTransaction {
        ScenarioDAO.findById(id)?.let { scenario ->
            // Delete all tasks first (due to foreign key constraints)
            ScenarioTaskTable.deleteWhere { ScenarioTaskTable.scenarioId eq id }

            // Delete scenario
            scenario.delete()
            true
        } ?: false
    }

}