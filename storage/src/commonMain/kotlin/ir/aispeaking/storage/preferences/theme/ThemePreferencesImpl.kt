package ir.aispeaking.storage.preferences.theme

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import org.koin.core.annotation.Single

@Single
class ThemePreferencesImpl(private val settings: Settings) : ThemePreferences {

    override fun saveThemeMode(modeKey: String) {
        settings.putString(SharedKeyConstant.THEME_MODE, modeKey)
    }

    override fun getThemeMode(): String {
        return settings.getString(SharedKeyConstant.THEME_MODE, "System")
    }
}
