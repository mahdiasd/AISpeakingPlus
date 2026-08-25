package ir.aispeaking.network.api.plan

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import ir.aispeaking.network.dto.plan.PlanResponse
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class PlanApiServiceImpl(private val httpClient: HttpClient) : PlanApiService {
    override suspend fun getAllPlans() =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/plan")
        }.body<NetworkResponse<List<PlanResponse>>>()
}