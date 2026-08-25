package ir.aispeaking.domain.repository.challenge

import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.user.User
import kotlinx.coroutines.flow.Flow

interface ChallengeRepository {
    suspend fun getChallenge(): Flow<DataResult<ChallengeSummary>>
    suspend fun createProgress(challengeId: String, score: Int): Flow<DataResult<User>>
}