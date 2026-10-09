package ir.aispeaking.network.api.tts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import ir.aispeaking.network.BuildConfig
import ir.aispeaking.network.NetworkConfig
import ir.aispeaking.network.model.NetworkResponse
import ir.aispeaking.network.model.tts.dto.SynthesizeRequestDto
import ir.aispeaking.network.model.tts.dto.SynthesizeResponseDto
import ir.aispeaking.network.model.tts.dto.TtsVoiceDto
import org.koin.core.annotation.Single

@Single
class TtsApi(
    private val client: HttpClient
) {
    private val baseUrl get() = NetworkConfig.baseUrl

    suspend fun getVoices(): NetworkResponse<List<TtsVoiceDto>> {
        return client.get("$baseUrl/api/v2/tts/voices").body()
    }

    suspend fun synthesize(request: SynthesizeRequestDto): NetworkResponse<SynthesizeResponseDto> {
        return client.post("$baseUrl/api/v2/tts/synthesize") {
            setBody(request)
        }.body()
    }

    suspend fun speak(text: String, voiceId: Int? = null, speed: Float? = null): ByteArray {
        return client.get("$baseUrl/api/v2/tts/speak") {
            parameter("text", text)
            if (voiceId != null) parameter("voiceId", voiceId)
            if (speed != null) parameter("speed", speed)
        }.body()
    }

    suspend fun getAudio(filename: String): ByteArray {
        return client.get("$baseUrl/api/v2/tts/audio/$filename").body()
    }
}
