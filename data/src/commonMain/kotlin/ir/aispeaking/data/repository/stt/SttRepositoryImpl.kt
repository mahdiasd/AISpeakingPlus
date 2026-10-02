package ir.aispeaking.data.repository.stt

import ir.aispeaking.data.mapper.stt.toDomain
import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.repository.stt.SttRepository
import ir.aispeaking.network.websocket.SttWebSocketClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class SttRepositoryImpl(
    private val sttWebSocketClient: SttWebSocketClient
) : SttRepository {

    override fun connectSttStream(): Flow<SttStreamEvent> {
        return sttWebSocketClient.connect().map { it.toDomain() }
    }

    override suspend fun sendAudioChunk(pcm16Bytes: ByteArray) {
        sttWebSocketClient.sendAudioChunk(pcm16Bytes)
    }

    override suspend fun closeSession() {
        sttWebSocketClient.close()
    }

    override fun isSessionActive(): Boolean {
        return sttWebSocketClient.isConnected
    }
}
