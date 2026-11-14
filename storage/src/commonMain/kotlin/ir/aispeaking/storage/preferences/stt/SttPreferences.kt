package ir.aispeaking.storage.preferences.stt

interface SttPreferences {
    fun saveSttEngine(engine: String)
    fun getSttEngine(): String
}
