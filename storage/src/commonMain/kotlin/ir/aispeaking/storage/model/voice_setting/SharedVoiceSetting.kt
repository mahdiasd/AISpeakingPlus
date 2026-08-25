package ir.aispeaking.storage.model.voice_setting

import kotlinx.serialization.Serializable

@Serializable
data class SharedVoiceSetting(
    val voiceId: Int = 0,
    val pitch: Float = 1f,
    val rate: Float = 1f,
    val showTranscribe: Boolean = true,
    val showSuggests: Boolean = true,
)
