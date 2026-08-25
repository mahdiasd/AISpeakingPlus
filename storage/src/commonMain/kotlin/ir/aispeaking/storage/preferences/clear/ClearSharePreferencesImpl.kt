package ir.aispeaking.storage.preferences.clear

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class ClearSharePreferencesImpl(private val settings: Settings) : ClearSharedPreferences {

    override fun clear() {
        // Remove specific keys from the multiplatform settings securely
        settings.remove(SharedKeyConstant.TOKEN)
        settings.remove(SharedKeyConstant.USER)
        settings.remove(SharedKeyConstant.SCENARIO)
        settings.remove(SharedKeyConstant.THEME_MODE)
        settings.remove(SharedKeyConstant.VOICE_SETTING)

        // Note: If you ever decide to clear absolutely EVERYTHING stored in settings,
        // you can simply use:
        // settings.clear()
    }
}