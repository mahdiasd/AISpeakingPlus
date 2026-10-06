package ir.aispeaking.domain.model.tts

enum class VoiceGender {
    FEMALE,
    MALE
}

enum class VoiceAccent {
    AMERICAN,
    BRITISH
}

data class TtsVoice(
    val id: Int,
    val code: String,
    val name: String,
    val gender: VoiceGender,
    val accent: VoiceAccent,
    val language: String = "en-US",
    val description: String
)

data class SynthesizeAudioResult(
    val audioUrl: String,
    val durationMs: Long,
    val sampleRate: Int,
    val voiceId: Int,
    val voiceName: String,
    val audioBytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SynthesizeAudioResult

        if (audioUrl != other.audioUrl) return false
        if (durationMs != other.durationMs) return false
        if (sampleRate != other.sampleRate) return false
        if (voiceId != other.voiceId) return false
        if (voiceName != other.voiceName) return false
        if (audioBytes != null) {
            if (other.audioBytes == null) return false
            if (!audioBytes.contentEquals(other.audioBytes)) return false
        } else if (other.audioBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = audioUrl.hashCode()
        result = 31 * result + durationMs.hashCode()
        result = 31 * result + sampleRate
        result = 31 * result + voiceId
        result = 31 * result + voiceName.hashCode()
        result = 31 * result + (audioBytes?.contentHashCode() ?: 0)
        return result
    }
}
