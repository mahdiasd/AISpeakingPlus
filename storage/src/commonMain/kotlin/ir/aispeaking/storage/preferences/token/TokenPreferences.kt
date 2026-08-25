package ir.aispeaking.storage.preferences.token

interface TokenPreferences {
    fun save(token: String)

    fun read(): String
}