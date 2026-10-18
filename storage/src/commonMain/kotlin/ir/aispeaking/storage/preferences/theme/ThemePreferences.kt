package ir.aispeaking.storage.preferences.theme

interface ThemePreferences {
    fun saveThemeMode(modeKey: String)
    fun getThemeMode(): String
}
