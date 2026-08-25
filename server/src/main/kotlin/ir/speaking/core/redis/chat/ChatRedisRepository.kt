package ir.speaking.core.redis.chat

import ir.speaking.feature.chat.model.Chat

interface ChatRedisRepository {
    suspend fun addChat(chat: Chat)

    suspend fun clearChats(userId: String, scenarioId: String)

    suspend fun getChats(userId: String, scenarioId: String): List<Chat>

    suspend fun getSystemPrompt(userId: String, scenarioId: String, isChallenge: Boolean): String
}