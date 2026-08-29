package ir.aispeaking.domain.usecase.chat

import ir.aispeaking.domain.model.chat.ChatStreamResult
import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class StreamChatUseCase(private val repo: ChatRepository) {
    operator fun invoke(
        scenarioId: String,
        message: String,
        isFirstMessage: Boolean,
        isChallenge: Boolean,
        englishLevel: String,
        starter: Role,
        voiceId: Int = 0,
        generateAudio: Boolean = true,
    ): Flow<ChatStreamResult> = repo.streamMessage(
        scenarioId = scenarioId,
        message = message,
        isFirstMessage = isFirstMessage,
        isChallenge = isChallenge,
        englishLevel = englishLevel,
        starter = starter,
        voiceId = voiceId,
        generateAudio = generateAudio,
    )
}
