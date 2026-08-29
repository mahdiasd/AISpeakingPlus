package ir.aispeaking.network.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class ChatStreamEvent {
    @Serializable
    @SerialName("chunk")
    data class Chunk(val text: String) : ChatStreamEvent()

    @Serializable
    @SerialName("done")
    data class Done(val chatResponse: ChatResponse) : ChatStreamEvent()

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : ChatStreamEvent()
}
