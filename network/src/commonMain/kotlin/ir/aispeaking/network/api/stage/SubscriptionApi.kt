package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.SubscribeRequestDto
import ir.aispeaking.network.model.stage.dto.SubscribeResponseDto
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

    suspend fun subscribe(request: SubscribeRequestDto): NetworkResponse<SubscribeResponseDto> {
        return client.post("$baseUrl/api/v2/subscriptions/subscribe") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

