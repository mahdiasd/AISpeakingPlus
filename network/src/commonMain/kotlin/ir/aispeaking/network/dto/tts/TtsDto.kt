package ir.aispeaking.network.dto.tts

import kotlinx.serialization.Serializable

@Serializable
data class TtsVoiceResponse(
    val id: Int,
    val code: String,
    val name: String,
    val gender: String,
    val accent: String,
    val language: String = "en-US",
    val description: String = ""
)

@Serializable
data class TtsSynthesizeRequest(
    val text: String,
    val voiceId: Int = 0,
    val speed: Float = 1.0f
)

@Serializable
data class TtsSynthesizeResponse(
    val audioUrl: String,
    val durationMs: Long? = null,
    val sampleRate: Int? = 24000,
    val voiceId: Int? = null,
    val voiceName: String? = null
)
