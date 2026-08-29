package ir.aispeaking.domain.model.chat

import ir.aispeaking.domain.model.error.AppError

sealed class ChatStreamResult {
    data class Chunk(val text: String) : ChatStreamResult()
    data class Done(val chat: Chat.Ai) : ChatStreamResult()
    data class Error(val error: AppError) : ChatStreamResult()
}
