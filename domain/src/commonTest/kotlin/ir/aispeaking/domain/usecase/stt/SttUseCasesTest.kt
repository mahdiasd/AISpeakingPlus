package ir.aispeaking.domain.usecase.stt

import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.repository.stt.SttRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SttUseCasesTest {

    private class FakeSttRepository : SttRepository {
        var closed = false
        val sentChunks = mutableListOf<ByteArray>()

        override fun connectSttStream(): Flow<SttStreamEvent> {
            return flowOf(
                SttStreamEvent.Ready("Stream ready"),
                SttStreamEvent.PartialTranscript("I want"),
                SttStreamEvent.FinalTranscript("I want to check in.")
            )
        }

        override suspend fun sendAudioChunk(pcm16Bytes: ByteArray) {
            sentChunks.add(pcm16Bytes)
        }

        override suspend fun closeSession() {
            closed = true
        }

        override fun isSessionActive(): Boolean = !closed
    }

    @Test
    fun testStreamSpeechToTextUseCase() = runTest {
        val repo = FakeSttRepository()
        val streamUseCase = StreamSpeechToTextUseCase(repo)
        val events = streamUseCase().toList()

        assertEquals(3, events.size)
        assertIs<SttStreamEvent.Ready>(events[0])
        assertIs<SttStreamEvent.PartialTranscript>(events[1])
        assertIs<SttStreamEvent.FinalTranscript>(events[2])
        assertEquals("I want to check in.", (events[2] as SttStreamEvent.FinalTranscript).text)
    }

    @Test
    fun testSendAudioChunkAndCloseSession() = runTest {
        val repo = FakeSttRepository()
        val sendChunkUseCase = SendAudioChunkUseCase(repo)
        val closeSessionUseCase = CloseSttSessionUseCase(repo)

        val audioData = byteArrayOf(10, 20, 30)
        sendChunkUseCase(audioData)
        assertEquals(1, repo.sentChunks.size)
        assertEquals(3, repo.sentChunks[0].size)

        closeSessionUseCase()
        assertTrue(repo.closed)
    }
}
