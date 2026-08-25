package ir.aispeaking.storage.preferences.theme_mode

import ir.aispeaking.storage.model.theme.SharedThemeMode

interface ThemeModePreferences {
    fun save(user: SharedThemeMode)

    fun read(): SharedThemeMode
}
