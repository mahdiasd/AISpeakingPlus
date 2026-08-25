package ir.aispeaking.network.api.category

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import ir.aispeaking.network.dto.category.CategoryResponse
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class CategoryApiServiceImpl(private val httpClient: HttpClient) : CategoryApiService {

    override suspend fun getCategories() =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/categories")
        }.body<NetworkResponse<List<CategoryResponse>>>()

}