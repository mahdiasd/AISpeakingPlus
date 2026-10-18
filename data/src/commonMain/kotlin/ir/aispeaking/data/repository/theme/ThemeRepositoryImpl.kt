package ir.aispeaking.data.repository.theme

import ir.aispeaking.domain.repository.theme.ThemeRepository
import ir.aispeaking.storage.preferences.theme.ThemePreferences
import org.koin.core.annotation.Single

@Single
class ThemeRepositoryImpl(
    private val themePreferences: ThemePreferences
) : ThemeRepository {

    override fun getThemeMode(): String {
        return themePreferences.getThemeMode()
    }

    override fun saveThemeMode(themeKey: String) {
        themePreferences.saveThemeMode(themeKey)
    }
}
