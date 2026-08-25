package ir.speaking.core.network.model;

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable;

// Response model for AvalAI API
@Serializable
data class AvalAiResponse(
    val id: String,
    @SerialName("object")
    val _object: String,
    val created: Long,
    val model: String,
    val choices: List<Choice>,
    val usage: Usage
)

@Serializable
data class Choice(
    val message: Message,
    val finish_reason: String,
    val index: Int
)

@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)