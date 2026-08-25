package ir.aispeaking.domain.usecase.guide

import ir.aispeaking.domain.repository.guide.GuideRepository
import org.koin.core.annotation.Single

@Single
class SetGuideForChat(
    private val guideRepository: GuideRepository
) {
    suspend operator fun invoke() {
        guideRepository.setChat()
    }
}
