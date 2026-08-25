package ir.aispeaking.network.api.tts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import ir.aispeaking.network.dto.tts.TtsSynthesizeRequest
import ir.aispeaking.network.dto.tts.TtsSynthesizeResponse
import ir.aispeaking.network.dto.tts.TtsVoiceResponse
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.platformBaseUrl
import org.koin.core.annotation.Single

@Single
class TtsApiServiceImpl(private val httpClient: HttpClient) : TtsApiService {
    override suspend fun getVoices(): NetworkResponse<List<TtsVoiceResponse>> =
        httpClient.get {
            url(platformBaseUrl() + "api/v1/tts/voices")
            timeout {
                requestTimeoutMillis = 15 * 1000
                connectTimeoutMillis = 15 * 1000
                socketTimeoutMillis = 15 * 1000
            }
        }.body<NetworkResponse<List<TtsVoiceResponse>>>()

    override suspend fun synthesize(request: TtsSynthesizeRequest): NetworkResponse<TtsSynthesizeResponse> =
        httpClient.post {
            url(platformBaseUrl() + "api/v1/tts/synthesize")
            setBody(request)
            timeout {
                requestTimeoutMillis = 30 * 1000
                connectTimeoutMillis = 30 * 1000
                socketTimeoutMillis = 30 * 1000
            }
        }.body<NetworkResponse<TtsSynthesizeResponse>>()
}
