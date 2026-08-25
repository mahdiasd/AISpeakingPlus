package ir.aispeaking.domain.usecase.guide

import ir.aispeaking.domain.model.guide.GuideCompletionStatus
import ir.aispeaking.domain.repository.guide.GuideRepository
import org.koin.core.annotation.Single

@Single
class ReadGuideStatusUseCase(
    private val guideRepository: GuideRepository
) {
    suspend operator fun invoke(): GuideCompletionStatus {
        return guideRepository.getGuidesCompletionStatus()
    }
}
