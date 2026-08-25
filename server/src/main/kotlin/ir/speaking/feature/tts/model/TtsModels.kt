package ir.speaking.feature.tts.model

import kotlinx.serialization.Serializable

@Serializable
enum class VoiceGender {
    FEMALE,
    MALE
}

@Serializable
enum class VoiceAccent {
    AMERICAN,
    BRITISH
}

@Serializable
data class TtsVoiceInfo(
    val id: Int,
    val code: String,
    val name: String,
    val gender: VoiceGender,
    val accent: VoiceAccent,
    val language: String = "en-US",
    val description: String
)

object KokoroVoices {
    val ALL: List<TtsVoiceInfo> = listOf(
        TtsVoiceInfo(0, "af", "Heart (Default)", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Clear, natural American female voice (recommended)"),
        TtsVoiceInfo(1, "af_bella", "Bella", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Warm, friendly American female voice"),
        TtsVoiceInfo(2, "af_nicole", "Nicole", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Energetic, bright American female voice"),
        TtsVoiceInfo(3, "af_sarah", "Sarah", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Expressive, calm American female voice"),
        TtsVoiceInfo(4, "af_sky", "Sky", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Soft, clear American female voice"),
        TtsVoiceInfo(5, "am_adam", "Adam", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Confident, clear American male voice"),
        TtsVoiceInfo(6, "am_michael", "Michael", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Warm, deep American male voice"),
        TtsVoiceInfo(7, "bf_emma", "Emma", VoiceGender.FEMALE, VoiceAccent.BRITISH, "en-GB", "Eloquent, crisp British female voice"),
        TtsVoiceInfo(8, "bf_isabella", "Isabella", VoiceGender.FEMALE, VoiceAccent.BRITISH, "en-GB", "Gentle, charming British female voice"),
        TtsVoiceInfo(9, "bm_george", "George", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Distinguished, classic British male voice"),
        TtsVoiceInfo(10, "bm_lewis", "Lewis", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Modern, clear British male voice")
    )

    fun findById(id: Int): TtsVoiceInfo =
        ALL.firstOrNull { it.id == id } ?: ALL.first()
}

@Serializable
data class SynthesizeRequest(
    val text: String,
    val voiceId: Int? = 0,
    val speed: Float? = 1.0f
)

@Serializable
data class SynthesizeResponse(
    val audioUrl: String,
    val durationMs: Long,
    val sampleRate: Int,
    val voiceId: Int,
    val voiceName: String
)

data class TtsResult(
    val filename: String,
    val audioUrl: String,
    val durationMs: Long,
    val sampleRate: Int,
    val voiceId: Int,
    val voiceName: String,
    val audioData: ByteArray? = null
)
