package ir.aispeaking.storage.preferences.onboarding

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class OnboardingPreferencesImpl(private val settings: Settings) : OnboardingPreferences {

    override fun save(value: Boolean) {
        settings.putBoolean(SharedKeyConstant.ONBOARDING, value)
    }

    override fun read(): Boolean {
        return settings.getBoolean(SharedKeyConstant.ONBOARDING, false)
    }
}
