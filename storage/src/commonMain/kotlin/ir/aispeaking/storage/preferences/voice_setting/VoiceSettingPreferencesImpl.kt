package ir.aispeaking.storage.preferences.voice_setting

import com.russhwolf.settings.Settings
import ir.aispeaking.storage.SharedKeyConstant
import ir.aispeaking.storage.model.voice_setting.SharedVoiceSetting
import ir.aispeaking.utils.fromJson
import ir.aispeaking.utils.toJson
import org.koin.core.annotation.Single

@Single
class VoiceSettingPreferencesImpl(private val settings: Settings) : VoiceSettingPreferences {

    override fun save(value: SharedVoiceSetting) {
        settings.putString(SharedKeyConstant.VOICE_SETTING, value.toJson() ?: "")
    }

    override fun read(): SharedVoiceSetting? {
        return settings.getStringOrNull(SharedKeyConstant.VOICE_SETTING).fromJson<SharedVoiceSetting>()
    }
}
