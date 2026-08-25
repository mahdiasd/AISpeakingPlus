package ir.aispeaking.data.repository.onboarding

import ir.aispeaking.domain.repository.onboarding.OnboardingRepository
import ir.aispeaking.storage.preferences.onboarding.OnboardingPreferences
import org.koin.core.annotation.Single

@Single
class OnboardingRepositoryImpl(
    private val preferences: OnboardingPreferences,
) : OnboardingRepository {

    override suspend fun onboardingIsSaw() = preferences.read()

    override suspend fun setOnboarding() {
        preferences.save(true)
    }
}