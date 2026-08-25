package ir.aispeaking.storage.preferences.firebase

interface FirebaseTokenPreferences {
    fun save(value: String)

    fun read(): String
}