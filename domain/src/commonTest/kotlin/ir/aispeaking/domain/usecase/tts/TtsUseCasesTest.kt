package ir.aispeaking.domain.usecase.tts

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.tts.SynthesizeAudioResult
import ir.aispeaking.domain.model.tts.TtsVoice
import ir.aispeaking.domain.model.tts.VoiceAccent
import ir.aispeaking.domain.model.tts.VoiceGender
import ir.aispeaking.domain.repository.tts.TtsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class TtsUseCasesTest {

    private val fakeVoices = listOf(
        TtsVoice(0, "af", "Heart", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Clear female"),
        TtsVoice(9, "bm_george", "George", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Classic British")
    )

    private val fakeRepo = object : TtsRepository {
        override suspend fun getVoices(): DataResult<List<TtsVoice>> =
            DataResult.Success(fakeVoices)

        override suspend fun synthesize(
            text: String,
            voiceId: Int?,
            speed: Float?
        ): DataResult<SynthesizeAudioResult> =
            DataResult.Success(
                SynthesizeAudioResult(
                    audioUrl = "/api/v2/tts/audio/speech.wav",
                    durationMs = 1500L,
                    sampleRate = 24000,
                    voiceId = voiceId ?: 0,
                    voiceName = "Heart"
                )
            )

        override suspend fun speak(
            text: String,
            voiceId: Int?,
            speed: Float?
        ): DataResult<ByteArray> =
            DataResult.Success(byteArrayOf(1, 2, 3))

        override suspend fun getAudioFile(filename: String): DataResult<ByteArray> =
            DataResult.Success(byteArrayOf(4, 5, 6))
    }

    @Test
    fun testGetTtsVoicesUseCase() = runTest {
        val useCase = GetTtsVoicesUseCase(fakeRepo)
        val result = useCase()
        assertIs<DataResult.Success<List<TtsVoice>>>(result)
        assertEquals(2, result.data.size)
        assertEquals("Heart", result.data[0].name)
    }

    @Test
    fun testSynthesizeSpeechUseCase() = runTest {
        val useCase = SynthesizeSpeechUseCase(fakeRepo)
        val result = useCase("Hello London", voiceId = 9, speed = 1.0f)
        assertIs<DataResult.Success<SynthesizeAudioResult>>(result)
        assertEquals(9, result.data.voiceId)
        assertEquals("/api/v2/tts/audio/speech.wav", result.data.audioUrl)
    }
}
