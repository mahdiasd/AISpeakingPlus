package ir.aispeaking.network.api.scenario

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.scenario.ScenarioDetailResponse
import ir.aispeaking.network.dto.scenario.ScenarioSummaryResponse
import ir.aispeaking.network.dto.user.UserResponse
import ir.aispeaking.network.model.NetworkResponse
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.core.annotation.Single

@Single
class ScenarioApiServiceImpl(private val httpClient: HttpClient) : ScenarioApiService {

    override suspend fun getScenario(id: String): NetworkResponse<ScenarioDetailResponse> =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/scenario")
            parameter("id", id)
        }.body<NetworkResponse<ScenarioDetailResponse>>()

    override suspend fun getScenarios(
        searchText: String?,
        categoryId: String?,
        page: Int,
        pageSize: Int?
    ): NetworkResponse<List<ScenarioSummaryResponse>> = httpClient.get {
        url(platformBaseUrl() + "api/v1/scenarios")
        parameter("searchText", searchText)
        parameter("categoryId", categoryId)
        parameter("page", page)
        parameter("pageSize", pageSize)
    }.body<NetworkResponse<List<ScenarioSummaryResponse>>>()

    override suspend fun getChallengeDetail(challengeId: String): NetworkResponse<ScenarioDetailResponse> {
        return httpClient.get {
            url(platformBaseUrl() + "api/v1/daily-challenge-detail")
            parameter("challengeId", challengeId)
        }.body<NetworkResponse<ScenarioDetailResponse>>()
    }

    override suspend fun createScenarioProgress(scenarioId: String, score: Int): NetworkResponse<UserResponse> {
        return httpClient.post {
            url(platformBaseUrl() + "api/v1/scenario-progress")
            setBody(buildJsonObject {
                put("scenarioId", scenarioId)
                put("score", score)
            })
        }.body<NetworkResponse<UserResponse>>()
    }

}