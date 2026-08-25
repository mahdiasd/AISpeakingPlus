package ir.aispeaking.network.api.chat

import ir.aispeaking.network.platformBaseUrl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.chat.ChatRequest
import ir.aispeaking.network.dto.chat.ChatResponse
import ir.aispeaking.network.dto.chat.ChatSuggestRequest
import ir.aispeaking.network.model.NetworkResponse
import org.koin.core.annotation.Single

@Single
class ChatApiServiceImpl(private val httpClient: HttpClient) : ChatApiService {
    override suspend fun sendMessage(chatRequest: ChatRequest): NetworkResponse<ChatResponse> =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/chat")
            setBody(chatRequest)
            timeout {
                requestTimeoutMillis = 30 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 30 * 1000
            }
        }.body<NetworkResponse<ChatResponse>>()

    override suspend fun getSuggestions(suggestRequest: ChatSuggestRequest) =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/chat/suggest")
            setBody(suggestRequest)
            timeout {
                requestTimeoutMillis = 30 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 30 * 1000
            }
        }.body<NetworkResponse<List<String>>>()
}