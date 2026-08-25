package ir.aispeaking.storage.preferences.theme_mode

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import ir.aispeaking.storage.model.theme.SharedThemeMode
import ir.aispeaking.utils.fromJson
import ir.aispeaking.utils.toJson
import org.koin.core.annotation.Single

@Single
class ThemeModePreferencesImpl(private val settings: Settings) : ThemeModePreferences {
    override fun save(user: SharedThemeMode) {
        settings.putString(SharedKeyConstant.THEME_MODE, user.toJson() ?: "")
    }

    override fun read(): SharedThemeMode {
        return settings.getStringOrNull(SharedKeyConstant.THEME_MODE)
            .fromJson<SharedThemeMode>() ?: SharedThemeMode("System")
    }
}
