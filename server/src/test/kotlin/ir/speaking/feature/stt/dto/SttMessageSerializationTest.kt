package ir.speaking.feature.stt.dto

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tests for the JSON wire format of [SttMessage] — verifies that the
 * sealed-class discriminator ("type") produces the exact JSON shapes the
 * WebSocket protocol expects, so clients can depend on a stable contract.
 *
 * These tests are pure-JVM and do not require native sherpa-onnx libraries.
 */
class SttMessageSerializationTest {

    private val json = Json {
        encodeDefaults = true
        explicitNulls = false
        classDiscriminator = "type"
    }

    @Test
    fun `partial transcript serializes with type=partial`() {
        val msg: SttMessage = SttMessage.PartialTranscript("hello world")
        val encoded = json.encodeToString(msg)

        assertTrue(encoded.contains("\"type\":\"partial\""),
            "must contain \"type\":\"partial\" — got: $encoded")
        assertTrue(encoded.contains("\"text\":\"hello world\""),
            "must contain the text field — got: $encoded")
    }

    @Test
    fun `final transcript serializes with type=final`() {
        val msg: SttMessage = SttMessage.FinalTranscript("the quick brown fox")
        val encoded = json.encodeToString(msg)

        assertTrue(encoded.contains("\"type\":\"final\""),
            "must contain \"type\":\"final\" — got: $encoded")
        assertTrue(encoded.contains("\"text\":\"the quick brown fox\""),
            "must contain the text field — got: $encoded")
    }

    @Test
    fun `error message serializes with type=error`() {
        val msg: SttMessage = SttMessage.ErrorMessage("server_busy")
        val encoded = json.encodeToString(msg)

        assertTrue(encoded.contains("\"type\":\"error\""),
            "must contain \"type\":\"error\" — got: $encoded")
        assertTrue(encoded.contains("\"message\":\"server_busy\""),
            "must contain the message field — got: $encoded")
    }

    @Test
    fun `ready message serializes with type=ready`() {
        val msg: SttMessage = SttMessage.ReadyMessage()
        val encoded = json.encodeToString(msg)

        assertTrue(encoded.contains("\"type\":\"ready\""),
            "must contain \"type\":\"ready\" — got: $encoded")
        assertTrue(encoded.contains("\"message\""),
            "must contain the message field — got: $encoded")
    }

    @Test
    fun `round-trip decode of partial transcript`() {
        val original: SttMessage = SttMessage.PartialTranscript("testing round trip")
        val encoded = json.encodeToString(SttMessage.serializer(), original)
        val decoded = json.decodeFromString(SttMessage.serializer(), encoded)

        assertTrue(decoded is SttMessage.PartialTranscript,
            "decoded should be PartialTranscript — got: ${decoded::class}")
        assertEquals("testing round trip",
            (decoded as SttMessage.PartialTranscript).text)
    }

    @Test
    fun `round-trip decode of final transcript`() {
        val original: SttMessage = SttMessage.FinalTranscript("final utterance")
        val encoded = json.encodeToString(SttMessage.serializer(), original)
        val decoded = json.decodeFromString(SttMessage.serializer(), encoded)

        assertTrue(decoded is SttMessage.FinalTranscript,
            "decoded should be FinalTranscript — got: ${decoded::class}")
        assertEquals("final utterance",
            (decoded as SttMessage.FinalTranscript).text)
    }

    @Test
    fun `round-trip decode of error message`() {
        val original: SttMessage = SttMessage.ErrorMessage("server_busy")
        val encoded = json.encodeToString(SttMessage.serializer(), original)
        val decoded = json.decodeFromString(SttMessage.serializer(), encoded)

        assertTrue(decoded is SttMessage.ErrorMessage,
            "decoded should be ErrorMessage — got: ${decoded::class}")
        assertEquals("server_busy",
            (decoded as SttMessage.ErrorMessage).message)
    }

    @Test
    fun `empty text is handled by partial transcript`() {
        val original: SttMessage = SttMessage.PartialTranscript("")
        val encoded = json.encodeToString(SttMessage.serializer(), original)
        val decoded = json.decodeFromString(SttMessage.serializer(), encoded)

        assertTrue(decoded is SttMessage.PartialTranscript)
        assertEquals("", (decoded as SttMessage.PartialTranscript).text)
    }

    /**
     * Verifies the exact JSON shape that the "server_busy" rejection must
     * produce. This is the contract the requirement specifies:
     *   {"type":"error","message":"server_busy"}
     */
    @Test
    fun `server_busy response has exact expected shape`() {
        val msg: SttMessage = SttMessage.ErrorMessage("server_busy")
        val encoded = json.encodeToString(msg)

        // The protocol requires this exact structure (whitespace-agnostic).
        val expected = """{"type":"error","message":"server_busy"}"""
        assertEquals(expected, encoded,
            "server_busy JSON must match the expected shape exactly")
    }
}
