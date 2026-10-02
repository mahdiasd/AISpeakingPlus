package ir.aispeaking.domain.usecase.stt

import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.repository.stt.SttRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class StreamSpeechToTextUseCase(
    private val sttRepository: SttRepository
) {
    operator fun invoke(): Flow<SttStreamEvent> {
        return sttRepository.connectSttStream()
    }
}

@Factory
class SendAudioChunkUseCase(
    private val sttRepository: SttRepository
) {
    suspend operator fun invoke(pcm16Bytes: ByteArray) {
        sttRepository.sendAudioChunk(pcm16Bytes)
    }
}

@Factory
class CloseSttSessionUseCase(
    private val sttRepository: SttRepository
) {
    suspend operator fun invoke() {
        sttRepository.closeSession()
    }
}
