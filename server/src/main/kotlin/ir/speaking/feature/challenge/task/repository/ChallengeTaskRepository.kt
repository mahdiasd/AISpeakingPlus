package ir.speaking.feature.challenge.task.repository

import ir.speaking.feature.challenge.task.model.ChallengeTask
import java.util.*

interface ChallengeTaskRepository {
    suspend fun read(challengeId: UUID): List<ChallengeTask>
    suspend fun create(challengeTask: ChallengeTask): ChallengeTask
    suspend fun delete(id: UUID): Boolean
}