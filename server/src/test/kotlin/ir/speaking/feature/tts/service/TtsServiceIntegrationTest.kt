package ir.speaking.feature.tts.service

import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsKokoroModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TtsServiceIntegrationTest {

    @Test
    fun testKokoroV1MultiLangInitializesAndSynthesizesMaleVoices() {
        val modelDir = listOf(
            File("models/kokoro-multi-lang-v1_0"),
            File("../models/kokoro-multi-lang-v1_0"),
            File("server/models/kokoro-multi-lang-v1_0")
        ).firstOrNull { it.exists() && File(it, "model.onnx").exists() }

        assertNotNull(modelDir, "kokoro-multi-lang-v1_0 model dir should exist")

        val modelFile = File(modelDir, "model.onnx")
        val voicesFile = File(modelDir, "voices.bin")
        val tokensFile = File(modelDir, "tokens.txt")
        val dataDir = File(modelDir, "espeak-ng-data")
        val lexiconUs = File(modelDir, "lexicon-us-en.txt")
        val lexiconGb = File(modelDir, "lexicon-gb-en.txt")

        val kokoroConfig = OfflineTtsKokoroModelConfig.builder()
            .setModel(modelFile.absolutePath)
            .setVoices(voicesFile.absolutePath)
            .setTokens(tokensFile.absolutePath)
            .setDataDir(dataDir.absolutePath)
            .setLexicon("${lexiconUs.absolutePath},${lexiconGb.absolutePath}")
            .setLengthScale(1.0f)
            .build()

        val modelConfig = OfflineTtsModelConfig.builder()
            .setKokoro(kokoroConfig)
            .setNumThreads(2)
            .setDebug(false)
            .setProvider("cpu")
            .build()

        val ttsConfig = OfflineTtsConfig.builder()
            .setModel(modelConfig)
            .setMaxNumSentences(1)
            .setSilenceScale(0.2f)
            .build()

        val offlineTts = OfflineTts(ttsConfig)
        assertEquals(24000, offlineTts.sampleRate)
        assertEquals(54, offlineTts.numSpeakers)

        // Test synthesis for male voices:
        // 14: am_fenrir, 17: am_onyx, 18: am_puck, 24: bm_daniel, 25: bm_fable
        val maleVoiceIds = listOf(13, 14, 15, 16, 17, 18, 24, 25, 26, 27)
        for (sid in maleVoiceIds) {
            val audio = offlineTts.generate("Hello, I am testing this English male voice.", sid, 1.0f)
            assertNotNull(audio)
            assertTrue(audio.samples.isNotEmpty(), "Audio samples for sid=$sid should not be empty")
            assertEquals(24000, audio.sampleRate)
            println("Successfully synthesized voice sid=$sid, sample count: ${audio.samples.size}")
        }
    }

    @Test
    fun testTtsServiceWithConfig() = kotlinx.coroutines.test.runTest {
        val config = TtsConfig.fromEnv()
        val service = TtsService(config)
        assertTrue(service.isAvailable, "Service should be initialized")
        val voices = service.getVoices()
        assertTrue(voices.size >= 28, "Voices should contain all English voices")
        val maleVoices = voices.filter { it.gender == ir.speaking.feature.tts.model.VoiceGender.MALE }
        assertTrue(maleVoices.size >= 13, "Should contain at least 13 male voices")

        val result = service.synthesize("Welcome to AI Speaking Plus!", sid = 17) // am_onyx
        assertNotNull(result)
        assertTrue(result.audioUrl.isNotEmpty())
        assertEquals("Onyx", result.voiceName)
    }
}
