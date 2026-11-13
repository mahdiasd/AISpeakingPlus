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
    private val sttWebSocketClient: SttWebSocketClient,
    private val sttPreferences: ir.aispeaking.storage.preferences.stt.SttPreferences? = null
) : SttRepository {

    override fun connectSttStream(): Flow<SttStreamEvent> {
        return sttWebSocketClient.connect().map { it.toDomain() }
    }

    override suspend fun sendAudioChunk(pcm16Bytes: ByteArray) {
        sttWebSocketClient.sendAudioChunk(pcm16Bytes)
    }

    override suspend fun sendStop() {
        sttWebSocketClient.sendStop()
    }

    override suspend fun closeSession() {
        sttWebSocketClient.close()
    }

    override fun isSessionActive(): Boolean {
        return sttWebSocketClient.isConnected
    }

    override fun getSttEngine(): String {
        return sttPreferences?.getSttEngine() ?: "app"
    }

    override fun saveSttEngine(engine: String) {
        sttPreferences?.saveSttEngine(engine)
    }
}
