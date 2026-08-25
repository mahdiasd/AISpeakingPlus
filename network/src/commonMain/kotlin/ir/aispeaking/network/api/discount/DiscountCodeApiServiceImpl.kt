package ir.aispeaking.network.api.discount

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import ir.aispeaking.network.dto.plan.PlanResponse
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class DiscountCodeApiServiceImpl(private val httpClient: HttpClient) : DiscountCodeApiService {
    override suspend fun checkDiscount(code: String): NetworkResponse<List<PlanResponse>> =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/discount-code/code/$code")
        }.body<NetworkResponse<List<PlanResponse>>>()
}