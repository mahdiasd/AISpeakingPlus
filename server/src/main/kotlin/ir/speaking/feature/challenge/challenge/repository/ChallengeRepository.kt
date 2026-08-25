package ir.speaking.feature.challenge.challenge.repository

import ir.speaking.feature.challenge.challenge.dto.SaveChallengeRequest
import ir.speaking.feature.challenge.challenge.model.Challenge
import java.util.*

interface ChallengeRepository {
    suspend fun readLastActive(): Challenge?
    suspend fun readLast(): Challenge?
    suspend fun get(id: String): Challenge?
    suspend fun create(saveChallengeRequest: SaveChallengeRequest): Challenge
    suspend fun update(saveChallengeRequest: SaveChallengeRequest): Challenge?
    suspend fun delete(id: UUID): Boolean
}
