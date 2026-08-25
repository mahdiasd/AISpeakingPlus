package ir.aispeaking.storage.preferences.voice_setting

import ir.aispeaking.storage.model.voice_setting.SharedVoiceSetting

interface VoiceSettingPreferences {

    fun save(value: SharedVoiceSetting)
    fun read(): SharedVoiceSetting?
}
