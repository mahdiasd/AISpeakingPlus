package ir.aispeaking.network.api.tts

import ir.aispeaking.network.dto.tts.TtsSynthesizeRequest
import ir.aispeaking.network.dto.tts.TtsSynthesizeResponse
import ir.aispeaking.network.dto.tts.TtsVoiceResponse
import ir.aispeaking.network.model.NetworkResponse

interface TtsApiService {
    suspend fun getVoices(): NetworkResponse<List<TtsVoiceResponse>>
    suspend fun synthesize(request: TtsSynthesizeRequest): NetworkResponse<TtsSynthesizeResponse>
}
