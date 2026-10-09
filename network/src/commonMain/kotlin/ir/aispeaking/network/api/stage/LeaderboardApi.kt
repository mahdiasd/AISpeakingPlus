package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.LeaderboardResponseDto
import org.koin.core.annotation.Single

@Single
class LeaderboardApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

    suspend fun getJourneyLeaderboard(page: Int = 1, pageSize: Int = 20): NetworkResponse<LeaderboardResponseDto> {
        return client.get("$baseUrl/api/v2/leaderboard/journey") {
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()
    }
}
