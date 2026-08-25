package ir.aispeaking.domain.usecase.onboarding

import ir.aispeaking.domain.repository.onboarding.OnboardingRepository
import org.koin.core.annotation.Single

@Single
class SetOnboardingUseCase(private val repo: OnboardingRepository) {
    suspend operator fun invoke() = repo.setOnboarding()
}