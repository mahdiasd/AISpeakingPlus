package ir.aispeaking.network.api.chat

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.preparePost
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import ir.aispeaking.network.dto.chat.ChatRequest
import ir.aispeaking.network.dto.chat.ChatResponse
import ir.aispeaking.network.dto.chat.ChatStreamEvent
import ir.aispeaking.network.dto.chat.ChatSuggestRequest
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.platformBaseUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single

@Single
class ChatApiServiceImpl(private val httpClient: HttpClient) : ChatApiService {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

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

    override fun streamMessage(chatRequest: ChatRequest): Flow<ChatStreamEvent> = channelFlow {
        try {
            httpClient.preparePost {
                url(platformBaseUrl() + "api/v1/chat/stream")
                setBody(chatRequest)
                timeout {
                    requestTimeoutMillis = 60 * 1000
                    connectTimeoutMillis = 15 * 1000
                    socketTimeoutMillis = 60 * 1000
                }
            }.execute { httpResponse ->
                val channel = httpResponse.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break
                    val trimmed = line.trim()
                    if (trimmed.startsWith("data:")) {
                        val payload = trimmed.removePrefix("data:").trim()
                        if (payload.isNotEmpty()) {
                            try {
                                val event = json.decodeFromString<ChatStreamEvent>(payload)
                                send(event)
                            } catch (_: Exception) {
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            send(ChatStreamEvent.Error(e.message ?: "Network error"))
        }
    }

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