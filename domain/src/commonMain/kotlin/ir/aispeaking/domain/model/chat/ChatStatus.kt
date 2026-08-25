package ir.aispeaking.domain.model.chat

sealed class ChatStatus {
    data object Failed : ChatStatus()
    data object Sending : ChatStatus()
    data class Answered(val grammar: String) : ChatStatus()
}