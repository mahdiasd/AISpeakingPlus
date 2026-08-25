package ir.speaking.feature.challenge.progress.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.challenge.progress.db.ChallengeProgressDAO
import ir.speaking.feature.challenge.progress.db.ChallengeProgressTable
import ir.speaking.feature.challenge.progress.db.toModel
import ir.speaking.feature.challenge.progress.model.ChallengeProgress
import ir.speaking.feature.user.repository.UserRepository
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.koin.core.annotation.Single
import java.util.*

@Single
class ChallengeProgressRepositoryImpl(
    private val userRepository: UserRepository
) : ChallengeProgressRepository {

    override suspend fun create(progress: ChallengeProgress): ChallengeProgress {
        userRepository.addScore(userId = progress.userId, score = progress.score)
        return suspendTransaction {
            ChallengeProgressDAO.new {
                userId = progress.userId
                challengeId = progress.challengeId
                score = progress.score
                completedAt = progress.completedAt
            }.toModel()
        }
    }

    override suspend fun update(progress: ChallengeProgress) =
        suspendTransaction {
            ChallengeProgressDAO.findByIdAndUpdate(progress.challengeId) {
                it.score = progress.score
                it.completedAt = progress.completedAt
            }?.toModel()
        }

    override suspend fun get(userId: UUID, challengeId: UUID) =
        suspendTransaction {
            ChallengeProgressDAO.find {
                (ChallengeProgressTable.userId eq userId) and
                        (ChallengeProgressTable.challengeId eq challengeId)
            }.firstOrNull()?.toModel()
        }


    override suspend fun getProgressByUserAndChallenge(userId: UUID, challengeId: UUID): ChallengeProgress? =
        suspendTransaction {
            ChallengeProgressDAO.find {
                (ChallengeProgressTable.userId eq userId) and
                        (ChallengeProgressTable.challengeId eq challengeId)
            }.firstOrNull()?.toModel()
        }

    override suspend fun countByUser(userId: UUID) = newSuspendedTransaction {
        ChallengeProgressTable.selectAll()
            .where { ChallengeProgressTable.userId eq userId }
            .count()
    }
}