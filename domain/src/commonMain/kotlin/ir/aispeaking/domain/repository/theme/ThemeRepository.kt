package ir.aispeaking.domain.repository.theme

interface ThemeRepository {
    fun getThemeMode(): String
    fun saveThemeMode(themeKey: String)
}
