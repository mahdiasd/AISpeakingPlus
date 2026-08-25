package ir.aispeaking.domain.repository.chat

import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.scenario.Role
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun sendMessage(
        scenarioId: String,
        message: String,
        isFirstMessage: Boolean,
        isChallenge: Boolean,
        englishLevel: String,
        starter: Role,
        voiceId: Int = 0,
        generateAudio: Boolean = true,
    ): Flow<DataResult<Chat>>

    suspend fun getSuggestions(
        lastAiMessage: String,
        tasks: List<String>,
        englishLevel: String,
        scenarioDescription: String,
    ): Flow<DataResult<List<String>>>
}