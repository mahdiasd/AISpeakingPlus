package ir.aispeaking.domain.repository.stt

import ir.aispeaking.domain.model.stt.SttStreamEvent
import kotlinx.coroutines.flow.Flow

interface SttRepository {
    fun connectSttStream(): Flow<SttStreamEvent>
    suspend fun sendAudioChunk(pcm16Bytes: ByteArray)
    suspend fun closeSession()
    fun isSessionActive(): Boolean
}
