package ir.speaking.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val grammar: Grammar? = null,
    val message: String,
    val translatedText: String? = null,
    val finishedTasksIndex: List<Int>? = null,
    val suggests: List<String>? = null,
    val audioUrl: String? = null,
    val voiceId: Int? = null,
    val durationMs: Long? = null
)

@Serializable
data class ChatSuggestResponse(
    val suggests: List<String>? = null
)

@Serializable
data class Grammar(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String? = null
)