package ir.aispeaking.data.mapper

import ir.aispeaking.data.mapper.stt.toDomain
import ir.aispeaking.data.mapper.tts.toDomain
import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.domain.model.tts.VoiceAccent
import ir.aispeaking.domain.model.tts.VoiceGender
import ir.aispeaking.network.model.stt.dto.SttMessageDto
import ir.aispeaking.network.model.tts.dto.SynthesizeResponseDto
import ir.aispeaking.network.model.tts.dto.TtsVoiceDto
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MappersTest {

    @Test
    fun testTtsVoiceDtoToDomain() {
        val dtoFemaleAmerican = TtsVoiceDto(
            id = 1,
            code = "af_sarah",
            name = "Sarah",
            gender = "FEMALE",
            accent = "AMERICAN",
            language = "en-US",
            description = "American female"
        )
        val domainFemale = dtoFemaleAmerican.toDomain()
        assertEquals(1, domainFemale.id)
        assertEquals("af_sarah", domainFemale.code)
        assertEquals("Sarah", domainFemale.name)
        assertEquals(VoiceGender.FEMALE, domainFemale.gender)
        assertEquals(VoiceAccent.AMERICAN, domainFemale.accent)

        val dtoMaleBritish = TtsVoiceDto(
            id = 9,
            code = "bm_george",
            name = "George",
            gender = "MALE",
            accent = "BRITISH",
            language = "en-GB",
            description = "British male"
        )
        val domainMale = dtoMaleBritish.toDomain()
        assertEquals(VoiceGender.MALE, domainMale.gender)
        assertEquals(VoiceAccent.BRITISH, domainMale.accent)
    }

    @Test
    fun testSynthesizeResponseDtoToDomain() {
        val dto = SynthesizeResponseDto(
            audioUrl = "/api/v2/tts/audio/speech_123.wav",
            durationMs = 2450L,
            sampleRate = 24000,
            voiceId = 3,
            voiceName = "Sarah"
        )
        val domain = dto.toDomain()
        assertEquals("/api/v2/tts/audio/speech_123.wav", domain.audioUrl)
        assertEquals(2450L, domain.durationMs)
        assertEquals(24000, domain.sampleRate)
        assertEquals(3, domain.voiceId)
        assertEquals("Sarah", domain.voiceName)
    }

    @Test
    fun testSttMessageDtoToDomain() {
        val readyDto = SttMessageDto.Ready("Stream ready.")
        val readyDomain = readyDto.toDomain()
        assertIs<SttStreamEvent.Ready>(readyDomain)
        assertEquals("Stream ready.", readyDomain.message)

        val partialDto = SttMessageDto.Partial("hello")
        val partialDomain = partialDto.toDomain()
        assertIs<SttStreamEvent.PartialTranscript>(partialDomain)
        assertEquals("hello", partialDomain.text)

        val finalDto = SttMessageDto.Final("hello world")
        val finalDomain = finalDto.toDomain()
        assertIs<SttStreamEvent.FinalTranscript>(finalDomain)
        assertEquals("hello world", finalDomain.text)

        val errorDto = SttMessageDto.Error("server_busy")
        val errorDomain = errorDto.toDomain()
        assertIs<SttStreamEvent.Error>(errorDomain)
        assertEquals("server_busy", errorDomain.message)
    }
}
