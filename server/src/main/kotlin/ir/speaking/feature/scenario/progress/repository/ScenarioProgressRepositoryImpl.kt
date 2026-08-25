package ir.speaking.feature.scenario.progress.repository

import ir.speaking.core.response.PagedList
import ir.speaking.core.response.PagingMeta
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.scenario.progress.db.ScenarioProgressDAO
import ir.speaking.feature.scenario.progress.db.ScenarioProgressTable
import ir.speaking.feature.scenario.progress.db.toModel
import ir.speaking.feature.scenario.progress.dto.response.ScenarioProgressResponse
import ir.speaking.feature.scenario.progress.dto.response.toResponse
import ir.speaking.feature.scenario.progress.model.ScenarioProgress
import ir.speaking.feature.user.repository.UserRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.util.*

@Single
class ScenarioProgressRepositoryImpl(val userRepository: UserRepository) : ScenarioProgressRepository {

    override suspend fun getProgressById(id: UUID): ScenarioProgress? =
        suspendTransaction {
            ScenarioProgressDAO.findById(id)?.toModel()
        }

    override suspend fun getProgressByUser(
        userId: UUID,
        page: Int,
        pageSize: Int
    ): PagedList<ScenarioProgressResponse> = suspendTransaction {
        val query = ScenarioProgressTable
            .selectAll()
            .where { ScenarioProgressTable.userId eq userId }
            .orderBy(ScenarioProgressTable.completedAt to SortOrder.DESC)

        val totalItems = query.count()

        PagedList(
            items = query
                .limit(pageSize)
                .offset(((page - 1) * pageSize).toLong())
                .map {
                    ScenarioProgressDAO.wrapRow(it).toModel().toResponse()
                },
            pagingMeta = PagingMeta(
                totalPages = ((totalItems + pageSize - 1) / pageSize).toInt(),
                totalItems = totalItems,
                page = page
            )
        )
    }

    override suspend fun createProgress(progress: ScenarioProgress): ScenarioProgress {
        userRepository.addScore(userId = progress.userId, score = progress.score)
        return suspendTransaction {
            ScenarioProgressDAO.new {
                userId = progress.userId
                scenarioId = progress.scenarioId
                score = progress.score
                completedAt = progress.completedAt
            }.toModel()
        }
    }

    override suspend fun updateProgress(progress: ScenarioProgress): ScenarioProgress? =
        suspendTransaction {
            ScenarioProgressDAO.findByIdAndUpdate(progress.id) {
                it.score = progress.score
                it.completedAt = progress.completedAt
            }?.toModel()
        }

    override suspend fun getProgressByUserAndScenario(userId: UUID, scenarioId: UUID): ScenarioProgress? =
        suspendTransaction {
            ScenarioProgressDAO.find {
                (ScenarioProgressTable.userId eq userId) and
                        (ScenarioProgressTable.scenarioId eq scenarioId)
            }.firstOrNull()?.toModel()
        }

    override suspend fun countByUser(userId: UUID) = newSuspendedTransaction {
        ScenarioProgressTable.selectAll()
            .where { ScenarioProgressTable.userId eq userId }
            .count()
    }
}