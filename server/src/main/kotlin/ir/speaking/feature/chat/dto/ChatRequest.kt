package ir.speaking.feature.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val scenarioId: String,

    val message: String,

    val isFirstMessage: Boolean,

    val isChallenge: Boolean = false,

    val englishLevel: String,

    val starter: String,

    val voiceId: Int? = null,

    val generateAudio: Boolean = true
)