package ir.aispeaking.network.api.config

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.config.ConfigRequest
import ir.aispeaking.network.dto.config.ConfigResponse
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.platformBaseUrl
import org.koin.core.annotation.Single

@Single
class ConfigApiServiceImpl(private val httpClient: HttpClient) : ConfigApiService {
    override suspend fun getConfig(configRequest: ConfigRequest): NetworkResponse<ConfigResponse> {
        return httpClient.post {
            url(platformBaseUrl() + "api/v1/config")
            setBody(configRequest)
        }.body<NetworkResponse<ConfigResponse>>()
    }
}