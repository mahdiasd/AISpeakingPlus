package ir.aispeaking.domain.repository.tts

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.tts.SynthesizeAudioResult
import ir.aispeaking.domain.model.tts.TtsVoice

interface TtsRepository {
    suspend fun getVoices(): DataResult<List<TtsVoice>>
    suspend fun synthesize(
        text: String,
        voiceId: Int? = null,
        speed: Float? = null
    ): DataResult<SynthesizeAudioResult>
    suspend fun speak(
        text: String,
        voiceId: Int? = null,
        speed: Float? = null
    ): DataResult<ByteArray>
    suspend fun getAudioFile(filename: String): DataResult<ByteArray>
}
