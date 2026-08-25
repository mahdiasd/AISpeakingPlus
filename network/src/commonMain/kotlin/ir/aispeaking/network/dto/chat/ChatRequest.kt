package ir.aispeaking.network.dto.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val scenarioId: String,
    val message: String,
    val isFirstMessage: Boolean,
    val isChallenge: Boolean,
    val englishLevel: String,
    val starter: String,
    val voiceId: Int? = 0,
    val generateAudio: Boolean? = true,
)

