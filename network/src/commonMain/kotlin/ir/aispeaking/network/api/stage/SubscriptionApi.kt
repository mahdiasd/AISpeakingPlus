package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.SubscriptionPlanDto
import ir.aispeaking.network.model.stage.dto.SubscriptionStatusDto
import org.koin.core.annotation.Single

@Single
class SubscriptionApi(
    private val client: HttpClient
) {
    private val baseUrl = BuildConfig.BaseUrl

    suspend fun getPlans(): NetworkResponse<List<SubscriptionPlanDto>> {
        return client.get("$baseUrl/api/v2/subscriptions/plans").body()
    }

    suspend fun getStatus(): NetworkResponse<SubscriptionStatusDto> {
        return client.get("$baseUrl/api/v2/subscriptions/status").body()
    }
}
