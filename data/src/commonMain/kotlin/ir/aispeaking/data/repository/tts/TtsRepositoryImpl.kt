package ir.aispeaking.data.repository.tts

import ir.aispeaking.data.mapper.tts.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.tts.SynthesizeAudioResult
import ir.aispeaking.domain.model.tts.TtsVoice
import ir.aispeaking.domain.repository.tts.TtsRepository
import ir.aispeaking.network.api.tts.TtsApi
import ir.aispeaking.network.model.tts.dto.SynthesizeRequestDto
import org.koin.core.annotation.Single

@Single
class TtsRepositoryImpl(
    private val ttsApi: TtsApi
) : TtsRepository {

    override suspend fun getVoices(): DataResult<List<TtsVoice>> {
        val result = safeCall { ttsApi.getVoices() }
        return when (result) {
            is DataResult.Success -> {
                val voices = result.data.map { it.toDomain() }
                DataResult.Success(data = voices, message = result.message)
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun synthesize(
        text: String,
        voiceId: Int?,
        speed: Float?
    ): DataResult<SynthesizeAudioResult> {
        val request = SynthesizeRequestDto(text = text, voiceId = voiceId, speed = speed)
        val result = safeCall { ttsApi.synthesize(request) }
        return when (result) {
            is DataResult.Success -> {
                DataResult.Success(data = result.data.toDomain(), message = result.message)
            }
            is DataResult.Failure -> DataResult.Failure(result.appError)
        }
    }

    override suspend fun speak(
        text: String,
        voiceId: Int?,
        speed: Float?
    ): DataResult<ByteArray> {
        return try {
            val audioBytes = ttsApi.speak(text = text, voiceId = voiceId, speed = speed)
            DataResult.Success(audioBytes)
        } catch (e: Throwable) {
            DataResult.Failure(
                NetworkError.InternalServer(httpStatus = 500, message = e.message ?: "Failed to stream audio")
            )
        }
    }

    override suspend fun getAudioFile(filename: String): DataResult<ByteArray> {
        return try {
            val audioBytes = ttsApi.getAudio(filename)
            DataResult.Success(audioBytes)
        } catch (e: Throwable) {
            DataResult.Failure(
                NetworkError.NotFound(httpStatus = 404, message = e.message ?: "Audio file not found")
            )
        }
    }
}
