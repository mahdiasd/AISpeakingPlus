package ir.aispeaking.domain.repository.onboarding

interface OnboardingRepository {
    suspend fun onboardingIsSaw(): Boolean
    suspend fun setOnboarding()
}