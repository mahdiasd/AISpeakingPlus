package ir.speaking.feature.challenge.task.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.challenge.task.db.ChallengeTaskDAO
import ir.speaking.feature.challenge.task.db.ChallengeTaskTable
import ir.speaking.feature.challenge.task.db.toModel
import ir.speaking.feature.challenge.task.model.ChallengeTask
import org.koin.core.annotation.Single
import java.util.*

@Single
class ChallengeTaskRepositoryImpl : ChallengeTaskRepository {
    override suspend fun read(challengeId: UUID): List<ChallengeTask> =
        suspendTransaction {
            ChallengeTaskDAO.find { ChallengeTaskTable.challengeId eq challengeId }.map { it.toModel() }
        }

    override suspend fun create(challengeTask: ChallengeTask): ChallengeTask =    suspendTransaction {
        ChallengeTaskDAO.new {
            challengeId = challengeTask.challengeId
            description = challengeTask.description
            persianDescription = challengeTask.persianDescription
            createdAt = challengeTask.createdAt
        }.toModel()
    }

    override suspend fun delete(id: UUID): Boolean =
        suspendTransaction {
            ChallengeTaskDAO.findById(id)?.let {
                it.delete()
                true
            } ?: false
        }
}