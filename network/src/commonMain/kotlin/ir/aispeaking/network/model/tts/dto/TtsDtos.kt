package ir.aispeaking.network.model.tts.dto

import kotlinx.serialization.Serializable

@Serializable
data class TtsVoiceDto(
    val id: Int,
    val code: String,
    val name: String,
    val gender: String,
    val accent: String,
    val language: String = "en-US",
    val description: String
)

@Serializable
data class SynthesizeRequestDto(
    val text: String,
    val voiceId: Int? = 0,
    val speed: Float? = 1.0f
)

@Serializable
data class SynthesizeResponseDto(
    val audioUrl: String,
    val durationMs: Long,
    val sampleRate: Int,
    val voiceId: Int,
    val voiceName: String
)
