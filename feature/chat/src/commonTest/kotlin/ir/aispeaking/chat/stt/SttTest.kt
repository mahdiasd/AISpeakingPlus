package ir.aispeaking.chat.stt

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SttTest {

    @Test
    fun testSttMessageDeserialization_Ready() {
        val jsonStr = """{"type":"ready","message":"Stream ready. Send mono PCM16 audio as binary frames."}"""
        val msg = sttJson.decodeFromString(SttMessage.serializer(), jsonStr)
        assertIs<SttMessage.Ready>(msg)
        assertEquals("Stream ready. Send mono PCM16 audio as binary frames.", msg.message)
    }

    @Test
    fun testSttMessageDeserialization_Partial() {
        val jsonStr = """{"type":"partial","text":"hello world"}"""
        val msg = sttJson.decodeFromString(SttMessage.serializer(), jsonStr)
        assertIs<SttMessage.Partial>(msg)
        assertEquals("hello world", msg.text)
    }

    @Test
    fun testSttMessageDeserialization_Final() {
        val jsonStr = """{"type":"final","text":"the quick brown fox"}"""
        val msg = sttJson.decodeFromString(SttMessage.serializer(), jsonStr)
        assertIs<SttMessage.Final>(msg)
        assertEquals("the quick brown fox", msg.text)
    }

    @Test
    fun testSttMessageDeserialization_Error() {
        val jsonStr = """{"type":"error","message":"server_busy"}"""
        val msg = sttJson.decodeFromString(SttMessage.serializer(), jsonStr)
        assertIs<SttMessage.Error>(msg)
        assertEquals("server_busy", msg.message)
    }

    @Test
    fun testSttMessageDeserialization_IgnoresUnknownKeys() {
        val jsonStr = """{"type":"partial","text":"testing","extra_field":123,"nested":{"a":true}}"""
        val msg = sttJson.decodeFromString(SttMessage.serializer(), jsonStr)
        assertIs<SttMessage.Partial>(msg)
        assertEquals("testing", msg.text)
    }

    @Test
    fun testBuildSttUrl_NativeWithoutQuery() {
        val url = SttClient.buildSttUrl(
            base = "http://localhost:8080",
            tokenInQuery = false,
            token = "jwt_token_123"
        )
        assertEquals("ws://localhost:8080/api/v1/stt", url)
    }

    @Test
    fun testBuildSttUrl_HttpsToWss() {
        val url = SttClient.buildSttUrl(
            base = "https://staging.aispeaking.ir",
            tokenInQuery = false,
            token = "jwt_token_123"
        )
        assertEquals("wss://staging.aispeaking.ir/api/v1/stt", url)
    }

    @Test
    fun testBuildSttUrl_WebWithQuery() {
        val url = SttClient.buildSttUrl(
            base = "https://staging.aispeaking.ir",
            tokenInQuery = true,
            token = "test_token"
        )
        assertTrue(url.startsWith("wss://staging.aispeaking.ir/api/v1/stt?token="))
    }

    @Test
    fun testSpeechTextFormatting() {
        fun formatSpeechText(input: String): String {
            return input
                .split(Regex("(?<=[.!?])\\s+"))
                .map { sentence ->
                    val trimmed = sentence.trim()
                    if (trimmed.isEmpty()) return@map ""

                    val capitalized = trimmed.replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase() else it.toString()
                    }

                    if (!capitalized.endsWith(".") && !capitalized.endsWith("!") && !capitalized.endsWith("?"))
                        "$capitalized."
                    else
                        capitalized
                }
                .joinToString(" ")
        }

        assertEquals("Hello world.", formatSpeechText("hello world"))
        assertEquals("How are you?", formatSpeechText("how are you?"))
        assertEquals("Great! Let us start.", formatSpeechText("great! let us start"))
    }
}
