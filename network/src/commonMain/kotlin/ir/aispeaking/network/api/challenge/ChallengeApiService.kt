package ir.aispeaking.network.api.challenge

import ir.aispeaking.network.dto.challenge.ChallengeSummaryResponse
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.network.model.NetworkResponse

interface ChallengeApiService {
    suspend fun getChallenge(): NetworkResponse<ChallengeSummaryResponse>

    suspend fun createProgress(challengeId: String, score: Int): NetworkResponse<UserResponse>
}