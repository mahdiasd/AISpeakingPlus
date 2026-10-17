package ir.speaking.feature.stt.service

import java.io.File
import java.io.FileInputStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SttServiceIntegrationTest {

    @Test
    fun testRealModelTranscribesAudio() {
        println("sherpa version: ${com.k2fsa.sherpa.onnx.VersionInfo.getVersion()}")
        val config = SttConfig.fromEnv()
        val service = SttService(config)

        val stream = service.tryAcquireStream()
        assertNotNull(stream, "Stream should be acquired")

        try {
            val wavFile = listOf(
                File("models/sherpa-onnx-streaming-zipformer-en-2023-06-26/test_wavs/0.wav"),
                File("../models/sherpa-onnx-streaming-zipformer-en-2023-06-26/test_wavs/0.wav"),
                File("models/sherpa-onnx-streaming-zipformer-en-2023-06-26/test_wavs/1.wav"),
                File("../models/sherpa-onnx-streaming-zipformer-en-2023-06-26/test_wavs/1.wav")
            ).firstOrNull { it.exists() }
            assertNotNull(wavFile, "Test wav file must exist")

            val waveReader = com.k2fsa.sherpa.onnx.WaveReader(wavFile.absolutePath)
            val samples = waveReader.samples

            val oneShotResult = service.transcribeWaveform(samples)
            assertNotNull(oneShotResult)
            assertTrue(oneShotResult.isNotBlank(), "One-shot transcript should not be blank")

            val chunkSize = 3200
            var offset = 0
            var accumulatedText = ""
            var partialEmitted = false
            while (offset < samples.size) {
                val end = (offset + chunkSize).coerceAtMost(samples.size)
                val chunk = samples.copyOfRange(offset, end)
                service.acceptWaveform(stream, chunk)
                while (service.isReady(stream)) {
                    service.decode(stream)
                }
                val rawResult = service.getRawResult(stream)
                val currentText = rawResult.text?.trim() ?: ""
                val isEndpoint = service.isEndpoint(stream)
                if (currentText.isNotBlank()) {
                    partialEmitted = true
                }
                if (isEndpoint && currentText.isNotBlank()) {
                    accumulatedText = if (accumulatedText.isEmpty()) currentText else "$accumulatedText $currentText"
                    service.reset(stream)
                }
                offset = end
            }

            service.inputFinished(stream)
            while (service.isReady(stream)) {
                service.decode(stream)
            }
            val remainingText = service.getText(stream)
            if (remainingText.isNotBlank()) {
                accumulatedText = if (accumulatedText.isEmpty()) remainingText else "$accumulatedText $remainingText"
            }

            assertTrue(partialEmitted, "Partial transcripts should have been emitted during streaming")
            assertTrue(accumulatedText.isNotBlank(), "Accumulated transcript should not be blank")
            // Verify that first letter is capitalized and not all uppercase
            assertTrue(accumulatedText[0].isUpperCase(), "First character should be uppercase")
            assertFalse(accumulatedText.all { !it.isLetter() || it.isUpperCase() }, "Transcript should not be all uppercase: '$accumulatedText'")
        } finally {
            service.releaseStream(stream)
        }
    }

    @Test
    fun testFormatTranscriptSentenceCasing() {
        val input1 = "AFTER EARLY NIGHTFALL THE YELLOW LAMPS WOULD LIGHT UP HERE AND THERE THE SQUALID QUARTER OF THE BROTHELS"
        val output1 = SttService.formatTranscript(input1)
        kotlin.test.assertEquals(
            "After early nightfall the yellow lamps would light up here and there the squalid quarter of the brothels",
            output1
        )

        val input2 = "HELLO HOW ARE YOU HOW CAN I HELP YOU"
        val output2 = SttService.formatTranscript(input2)
        kotlin.test.assertEquals("Hello how are you how can I help you", output2)

        val input3 = "I AM FINE THANK YOU AND I WANT TO GO TO LONDON ON MONDAY"
        val output3 = SttService.formatTranscript(input3)
        kotlin.test.assertEquals("I am fine thank you and I want to go to London on Monday", output3)

        val input4 = "OK I UNDERSTAND THAT IT IS AI AND TV"
        val output4 = SttService.formatTranscript(input4)
        kotlin.test.assertEquals("OK I understand that it is AI and TV", output4)

        val input5 = "I'M DOING GREAT DON'T WORRY"
        val output5 = SttService.formatTranscript(input5)
        kotlin.test.assertEquals("I'm doing great don't worry", output5)
    }
}
