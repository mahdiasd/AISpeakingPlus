package ir.aispeaking.network.api.chat

import ir.aispeaking.network.dto.chat.ChatRequest
import ir.aispeaking.network.dto.chat.ChatResponse
import ir.aispeaking.network.dto.chat.ChatSuggestRequest
import ir.aispeaking.network.model.NetworkResponse


import ir.aispeaking.network.dto.chat.ChatStreamEvent
import kotlinx.coroutines.flow.Flow

interface ChatApiService {
    suspend fun sendMessage(chatRequest : ChatRequest): NetworkResponse<ChatResponse>

    fun streamMessage(chatRequest: ChatRequest): Flow<ChatStreamEvent>

    suspend fun getSuggestions(suggestRequest: ChatSuggestRequest): NetworkResponse<List<String>>
}