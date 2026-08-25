package ir.aispeaking.network.api.challenge

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.challenge.ChallengeSummaryResponse
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.network.model.NetworkResponse
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Single

@Single
class ChallengeApiServiceImpl(private val httpClient: HttpClient) : ChallengeApiService {

    override suspend fun getChallenge(): NetworkResponse<ChallengeSummaryResponse> {
        return httpClient.get {
            url(platformBaseUrl() + "api/v1/daily-challenge")
        }.body<NetworkResponse<ChallengeSummaryResponse>>()
    }

    override suspend fun createProgress(challengeId: String, score: Int): NetworkResponse<UserResponse> {
        return httpClient.post {
            url(platformBaseUrl() + "api/v1/challenge-progress")
            setBody(buildJsonObject {
                put("challengeId", challengeId)
                put("score", score)
            })
        }.body<NetworkResponse<UserResponse>>()
    }
}