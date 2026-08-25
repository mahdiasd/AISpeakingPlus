package ir.speaking.feature.chat.dto

import kotlinx.serialization.Serializable

@Serializable
data class ChatSuggestRequest(
    val lastAiMessage: String,
    val tasks: List<String>,
    val englishLevel: String,
    val scenarioDescription: String,
)