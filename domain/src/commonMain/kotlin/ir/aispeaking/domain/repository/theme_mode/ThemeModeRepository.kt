package ir.aispeaking.domain.repository.theme_mode

import ir.aispeaking.domain.model.theme_mode.ThemeMode

interface ThemeModeRepository {
    suspend fun save(mode: ThemeMode)

    suspend fun read(): ThemeMode
}