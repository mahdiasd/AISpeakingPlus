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
    val V0_ALL: List<TtsVoiceInfo> = listOf(
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

    // Complete Kokoro v1.0 English voice catalog (28 English voices, including 13 male voices)
    val V1_ENGLISH: List<TtsVoiceInfo> = listOf(
        // Recommended / Default Female
        TtsVoiceInfo(3, "af_heart", "Heart (Default)", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Clear, exceptionally natural American female voice (recommended)"),
        TtsVoiceInfo(2, "af_bella", "Bella", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Warm, friendly American female voice (top-rated)"),
        TtsVoiceInfo(6, "af_nicole", "Nicole", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Energetic, bright American female voice"),
        TtsVoiceInfo(9, "af_sarah", "Sarah", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Expressive, calm American female voice"),
        TtsVoiceInfo(10, "af_sky", "Sky", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Soft, clear American female voice"),
        TtsVoiceInfo(0, "af_alloy", "Alloy", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Clear, neutral American female voice"),
        TtsVoiceInfo(1, "af_aoede", "Aoede", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Melodic, conversational American female voice"),
        TtsVoiceInfo(4, "af_jessica", "Jessica", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Crisp, lively American female voice"),
        TtsVoiceInfo(5, "af_kore", "Kore", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Gentle, composed American female voice"),
        TtsVoiceInfo(7, "af_nova", "Nova", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Modern, radiant American female voice"),
        TtsVoiceInfo(8, "af_river", "River", VoiceGender.FEMALE, VoiceAccent.AMERICAN, "en-US", "Relaxed, natural American female voice"),

        // American Male Voices (9 voices)
        TtsVoiceInfo(14, "am_fenrir", "Fenrir", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Deep, resonant, premium narrative American male voice (top-rated)"),
        TtsVoiceInfo(17, "am_onyx", "Onyx", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Deep, authoritative, smooth baritone male voice"),
        TtsVoiceInfo(16, "am_michael", "Michael", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Warm, deep American male voice (top-rated)"),
        TtsVoiceInfo(18, "am_puck", "Puck", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Expressive, charismatic American male voice (top-rated)"),
        TtsVoiceInfo(13, "am_eric", "Eric", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Conversational, upbeat, natural American male voice"),
        TtsVoiceInfo(15, "am_liam", "Liam", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Youthful, friendly and warm American male voice"),
        TtsVoiceInfo(12, "am_echo", "Echo", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Balanced, modern conversational American male voice"),
        TtsVoiceInfo(11, "am_adam", "Adam", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Confident, clear American male voice"),
        TtsVoiceInfo(19, "am_santa", "Santa", VoiceGender.MALE, VoiceAccent.AMERICAN, "en-US", "Jovial, rich character male voice"),

        // British Female Voices (4 voices)
        TtsVoiceInfo(21, "bf_emma", "Emma", VoiceGender.FEMALE, VoiceAccent.BRITISH, "en-GB", "Eloquent, crisp British female voice"),
        TtsVoiceInfo(22, "bf_isabella", "Isabella", VoiceGender.FEMALE, VoiceAccent.BRITISH, "en-GB", "Gentle, charming British female voice"),
        TtsVoiceInfo(20, "bf_alice", "Alice", VoiceGender.FEMALE, VoiceAccent.BRITISH, "en-GB", "Refined, articulate British female voice"),
        TtsVoiceInfo(23, "bf_lily", "Lily", VoiceGender.FEMALE, VoiceAccent.BRITISH, "en-GB", "Bright, melodious British female voice"),

        // British Male Voices (4 voices)
        TtsVoiceInfo(25, "bm_fable", "Fable", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Distinguished storyteller, rich British male voice (top-rated)"),
        TtsVoiceInfo(24, "bm_daniel", "Daniel", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Articulate, authoritative British male voice (RP)"),
        TtsVoiceInfo(26, "bm_george", "George", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Classic, mature British gentleman voice (top-rated)"),
        TtsVoiceInfo(27, "bm_lewis", "Lewis", VoiceGender.MALE, VoiceAccent.BRITISH, "en-GB", "Modern, upbeat British male voice")
    )

    val ALL: List<TtsVoiceInfo> = V1_ENGLISH

    fun findById(id: Int, isV1: Boolean = true): TtsVoiceInfo {
        val list = if (isV1) V1_ENGLISH else V0_ALL
        return list.firstOrNull { it.id == id } ?: list.first()
    }

    fun findByCode(code: String?, isV1: Boolean = true): TtsVoiceInfo? {
        if (code == null) return null
        val normalized = code.trim().lowercase()
        // Handle aliases
        val targetCode = when (normalized) {
            "af", "heart" -> if (isV1) "af_heart" else "af"
            else -> normalized
        }
        val list = if (isV1) V1_ENGLISH else V0_ALL
        return list.firstOrNull { it.code.equals(targetCode, ignoreCase = true) }
            ?: (if (isV1) V0_ALL.firstOrNull { it.code.equals(targetCode, ignoreCase = true) } else null)
    }
}

@Serializable
data class SynthesizeRequest(
    val text: String,
    val voiceId: Int? = 0,
    val speed: Float? = 1.0f
)

typealias TtsSynthesizeRequest = SynthesizeRequest

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
