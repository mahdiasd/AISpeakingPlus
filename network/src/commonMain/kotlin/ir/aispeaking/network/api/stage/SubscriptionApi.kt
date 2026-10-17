package ir.aispeaking.network.api.stage

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.stage.dto.SubscribeRequestDto
import ir.aispeaking.network.model.stage.dto.SubscribeResponseDto
import ir.aispeaking.network.model.stage.dto.SubscriptionPlanDto
import ir.aispeaking.network.model.stage.dto.SubscriptionStatusDto
import ir.aispeaking.network.model.stage.dto.ValidatePromoRequestDto
import ir.aispeaking.network.model.stage.dto.ValidatePromoResponseDto
import ir.aispeaking.network.model.stage.dto.DirectCheckoutRequestDto
import ir.aispeaking.network.model.stage.dto.DirectCheckoutResponseDto
import ir.aispeaking.network.model.stage.dto.VerifyMarketPurchaseRequestDto
import ir.aispeaking.network.model.stage.dto.VerifyMarketPurchaseResponseDto
import org.koin.core.annotation.Single

@Single
class SubscriptionApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

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

    suspend fun validatePromo(request: ValidatePromoRequestDto): NetworkResponse<ValidatePromoResponseDto> {
        return client.post("$baseUrl/api/v2/subscriptions/validate-promo") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun verifyMarketPurchase(request: VerifyMarketPurchaseRequestDto): NetworkResponse<VerifyMarketPurchaseResponseDto> {
        return client.post("$baseUrl/api/v2/subscriptions/verify-market-purchase") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun directCheckout(request: DirectCheckoutRequestDto): NetworkResponse<DirectCheckoutResponseDto> {
        return client.post("$baseUrl/api/v2/subscriptions/direct-checkout") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}

