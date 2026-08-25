package ir.aispeaking.network.api.translation

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.translate.TranslationResponse
import ir.aispeaking.network.dto.translate.UpsertTranslationRequest
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class TranslationApiServiceImpl(private val httpClient: HttpClient) : TranslationApiService {

    override suspend fun create(upsertTranslationRequest: UpsertTranslationRequest) =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/translation")
            setBody(upsertTranslationRequest)
        }.body<NetworkResponse<TranslationResponse>>()

    override suspend fun update(upsertTranslationRequest: UpsertTranslationRequest) =
        httpClient.put {
            url(platformBaseUrl() + "api/v1/translation")
            setBody(upsertTranslationRequest)
        }.body<NetworkResponse<TranslationResponse>>()

    override suspend fun delete(id: String) =
        httpClient.delete {
            url(platformBaseUrl() + "api/v1/translation/${id}")
        }.body<NetworkResponse<Boolean>>()

    override suspend fun getTranslations(searchText: String?, page: Int, pageSize: Int?) = httpClient.get {
        url(platformBaseUrl() + "api/v1/translation")
        parameter("searchText", searchText)
        parameter("page", page)
        parameter("pageSize", pageSize)
    }.body<NetworkResponse<List<TranslationResponse>>>()

}