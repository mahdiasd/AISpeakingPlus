package ir.aispeaking.network.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val grammar: GrammarResponse? = null,
    val message: String,
    val translatedText: String? = null,
    val finishedTasksIndex: List<Int>? = null,
    val suggests: List<String>? = null,
    val audioUrl: String? = null,
    val voiceId: Int? = null,
    val durationMs: Long? = null
)

@Serializable
data class GrammarResponse(
    @SerialName("status")
    val status: String,

    @SerialName("message")
    val message: String? = null
)