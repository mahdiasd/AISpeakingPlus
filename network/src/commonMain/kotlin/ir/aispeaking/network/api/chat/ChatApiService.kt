package ir.aispeaking.network.api.chat

import ir.aispeaking.network.dto.chat.ChatRequest
import ir.aispeaking.network.dto.chat.ChatResponse
import ir.aispeaking.network.dto.chat.ChatSuggestRequest
import ir.aispeaking.network.model.NetworkResponse


interface ChatApiService {
    suspend fun sendMessage(chatRequest : ChatRequest): NetworkResponse<ChatResponse>

    suspend fun getSuggestions(suggestRequest: ChatSuggestRequest): NetworkResponse<List<String>>

}