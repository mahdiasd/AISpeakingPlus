package ir.aispeaking.domain.usecase.chat

import ir.aispeaking.domain.model.scenario.Role
import ir.aispeaking.domain.repository.chat.ChatRepository
import org.koin.core.annotation.Single

@Single
class SendChatUseCase(private val repo: ChatRepository) {
    suspend operator fun invoke(
        scenarioId: String,
        message: String,
        isFirstMessage: Boolean,
        isChallenge: Boolean,
        englishLevel: String,
        starter: Role,
        voiceId: Int = 0,
        generateAudio: Boolean = true,
    ) = repo.sendMessage(
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