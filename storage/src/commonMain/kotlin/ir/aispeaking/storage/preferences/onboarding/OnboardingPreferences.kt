package ir.aispeaking.storage.preferences.onboarding

interface OnboardingPreferences {
    fun save(value: Boolean)

    fun read(): Boolean
}