package ir.aispeaking.data.mapper.chat

import ir.aispeaking.domain.model.chat.AiVoiceState
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.network.dto.chat.ChatResponse
import kotlinx.collections.immutable.toImmutableList

fun ChatResponse.toDomain(): Chat {
    return Chat.Ai(
        message = this.message,
        translatedMessage = this.translatedText,
        voiceState = AiVoiceState.PendingToPlay,
        grammar = if (grammar != null && grammar!!.status.equals("incorrect", true)) {
            grammar!!.message ?: ""
        } else "",
        finishTaskIndexes = finishedTasksIndex?.toImmutableList() ?: listOf<Int>().toImmutableList(),
        suggests = suggests?.toImmutableList(),
        audioUrl = this.audioUrl
    )
}