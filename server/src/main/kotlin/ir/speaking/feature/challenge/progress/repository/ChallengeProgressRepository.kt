package ir.speaking.feature.challenge.progress.repository

import ir.speaking.feature.challenge.progress.model.ChallengeProgress
import java.util.*

interface ChallengeProgressRepository {
    suspend fun create(progress: ChallengeProgress): ChallengeProgress
    suspend fun update(progress: ChallengeProgress): ChallengeProgress?
    suspend fun get(userId: UUID, challengeId: UUID): ChallengeProgress?
    suspend fun getProgressByUserAndChallenge(userId: UUID, challengeId: UUID): ChallengeProgress?
    suspend fun countByUser(userId: UUID): Long
}