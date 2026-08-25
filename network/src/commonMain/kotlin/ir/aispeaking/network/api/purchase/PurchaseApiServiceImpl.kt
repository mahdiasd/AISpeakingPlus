package ir.aispeaking.network.api.purchase

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import ir.aispeaking.network.dto.purchase.PurchaseRequest
import ir.aispeaking.network.dto.purchase.PurchaseResponse
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class PurchaseApiServiceImpl(private val httpClient: HttpClient) : PurchaseApiService {

    override suspend fun createPurchase(request: PurchaseRequest) =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/purchase")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<NetworkResponse<PurchaseResponse>>()

    override suspend fun getPurchasesByUserId() =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/purchase/user")
        }.body<NetworkResponse<List<PurchaseResponse>>>()

    override suspend fun getActiveUserPurchases() =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/purchase/user/active")
        }.body<NetworkResponse<List<PurchaseResponse>>>()

}