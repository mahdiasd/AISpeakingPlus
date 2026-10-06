package ir.aispeaking.network

import ir.aispeaking.network.model.stt.dto.SttMessageDto
import ir.aispeaking.network.model.tts.dto.SynthesizeRequestDto
import ir.aispeaking.network.model.tts.dto.SynthesizeResponseDto
import ir.aispeaking.network.model.tts.dto.TtsVoiceDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SttMessagesSerializationTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        classDiscriminator = "type"
    }

    @Test
    fun testDecodeReadyMessage() {
        val payload = """{"type":"ready","message":"Stream ready. Send mono PCM16 audio."}"""
        val message = json.decodeFromString<SttMessageDto>(payload)

        assertIs<SttMessageDto.Ready>(message)
        assertEquals("Stream ready. Send mono PCM16 audio.", message.message)
    }

    @Test
    fun testDecodePartialTranscript() {
        val payload = """{"type":"partial","text":"Hello world"}"""
        val message = json.decodeFromString<SttMessageDto>(payload)

        assertIs<SttMessageDto.Partial>(message)
        assertEquals("Hello world", message.text)
    }

    @Test
    fun testDecodeFinalTranscript() {
        val payload = """{"type":"final","text":"I would like a window seat please."}"""
        val message = json.decodeFromString<SttMessageDto>(payload)

        assertIs<SttMessageDto.Final>(message)
        assertEquals("I would like a window seat please.", message.text)
    }

    @Test
    fun testDecodeErrorMessage() {
        val payload = """{"type":"error","message":"server_busy"}"""
        val message = json.decodeFromString<SttMessageDto>(payload)

        assertIs<SttMessageDto.Error>(message)
        assertEquals("server_busy", message.message)
    }

    @Test
    fun testTtsDtosSerialization() {
        val request = SynthesizeRequestDto(text = "Hello London", voiceId = 3, speed = 1.0f)
        val encoded = json.encodeToString(request)
        val decoded = json.decodeFromString<SynthesizeRequestDto>(encoded)

        assertEquals("Hello London", decoded.text)
        assertEquals(3, decoded.voiceId)
        assertEquals(1.0f, decoded.speed)

        val voiceJson = """{"id":0,"code":"af","name":"Heart","gender":"FEMALE","accent":"AMERICAN","language":"en-US","description":"Clear voice"}"""
        val voice = json.decodeFromString<TtsVoiceDto>(voiceJson)
        assertEquals(0, voice.id)
        assertEquals("af", voice.code)
        assertEquals("FEMALE", voice.gender)
    }
}
