package ir.aispeaking.domain.usecase.welcome_message

import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.domain.model.guide.GuideCompletionStatus
import ir.aispeaking.domain.repository.guide.GuideRepository
import ir.aispeaking.domain.repository.welcome_message.WelcomeMessageRepository
import org.koin.core.annotation.Single

@Single
class ReadWelcomeMessageUseCase(
    private val guideRepository: WelcomeMessageRepository
) {
    suspend operator fun invoke(): WelcomeMessage? {
        return guideRepository.read()
    }
}
