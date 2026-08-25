package ir.aispeaking.domain.usecase.chat

import ir.aispeaking.domain.repository.chat.ChatRepository
import org.koin.core.annotation.Single

@Single
class GetChatSuggestionsUseCase(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(
        lastAiMessage: String,
        tasks: List<String>,
        englishLevel: String,
        scenarioDescription: String,
    ) =
        chatRepository.getSuggestions(
            lastAiMessage = lastAiMessage,
            tasks = tasks,
            englishLevel = englishLevel,
            scenarioDescription = scenarioDescription,
        )
}