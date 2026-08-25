package ir.aispeaking.domain.usecase.welcome_message

import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.domain.repository.welcome_message.WelcomeMessageRepository
import org.koin.core.annotation.Single

@Single
class SaveWelcomeMessageUseCase(
    private val repo: WelcomeMessageRepository
) {
    suspend operator fun invoke(value: WelcomeMessage?) {
        return repo.save(value)
    }
}
