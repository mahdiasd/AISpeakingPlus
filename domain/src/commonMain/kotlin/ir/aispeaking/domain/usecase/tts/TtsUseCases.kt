package ir.aispeaking.domain.usecase.tts

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.tts.SynthesizeAudioResult
import ir.aispeaking.domain.model.tts.TtsVoice
import ir.aispeaking.domain.repository.tts.TtsRepository
import org.koin.core.annotation.Factory

@Factory
class GetTtsVoicesUseCase(
    private val ttsRepository: TtsRepository
) {
    suspend operator fun invoke(): DataResult<List<TtsVoice>> {
        return ttsRepository.getVoices()
    }
}

@Factory
class SynthesizeSpeechUseCase(
    private val ttsRepository: TtsRepository
) {
    suspend operator fun invoke(
        text: String,
        voiceId: Int? = null,
        speed: Float? = null
    ): DataResult<SynthesizeAudioResult> {
        return ttsRepository.synthesize(text, voiceId, speed)
    }
}

@Factory
class StreamAudioDirectUseCase(
    private val ttsRepository: TtsRepository
) {
    suspend operator fun invoke(
        text: String,
        voiceId: Int? = null,
        speed: Float? = null
    ): DataResult<ByteArray> {
        return ttsRepository.speak(text, voiceId, speed)
    }
}
