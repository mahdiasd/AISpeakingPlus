package ir.aispeaking.domain.usecase.guide

import ir.aispeaking.domain.repository.guide.GuideRepository
import org.koin.core.annotation.Single

@Single
class IsGuideCompletedUseCase(private val repository: GuideRepository) {
    operator fun invoke(screenKey: String): Boolean = repository.isGuideCompleted(screenKey)
}

@Single
class MarkGuideCompletedUseCase(private val repository: GuideRepository) {
    operator fun invoke(screenKey: String) = repository.markGuideCompleted(screenKey)
}

@Single
class ResetGuideUseCase(private val repository: GuideRepository) {
    operator fun invoke(screenKey: String) = repository.resetGuide(screenKey)
    fun all() = repository.resetAllGuides()
}
