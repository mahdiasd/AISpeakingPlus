package ir.aispeaking.data.repository.theme_mode

import ir.aispeaking.domain.model.theme_mode.ThemeMode
import ir.aispeaking.domain.repository.theme_mode.ThemeModeRepository
import ir.aispeaking.storage.model.theme.SharedThemeMode
import ir.aispeaking.storage.preferences.theme_mode.ThemeModePreferences
import org.koin.core.annotation.Single

@Single
class ThemeModeRepositoryImpl(
    private val pref: ThemeModePreferences,
) : ThemeModeRepository {

    override suspend fun save(mode: ThemeMode) {
        pref.save(SharedThemeMode(mode.mode))
    }

    override suspend fun read(): ThemeMode {
        return ThemeMode(pref.read().mode)
    }

}