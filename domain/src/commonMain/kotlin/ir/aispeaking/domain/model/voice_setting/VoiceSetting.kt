package ir.aispeaking.domain.model.voice_setting

data class VoiceSetting(
    val voiceId: Int = 0,
    val speed: Float = 1f,
    val pitch: Float = 1f,
    val showTranscribe: Boolean = true,
    val showSuggests: Boolean = true
)
