package ir.aispeaking.data.repository.voice_setting

import ir.aispeaking.data.mapper.voice_setting.toDomain
import ir.aispeaking.data.mapper.voice_setting.toShared
import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.domain.repository.voice_setting.VoiceSettingRepository
import ir.aispeaking.storage.preferences.voice_setting.VoiceSettingPreferences
import org.koin.core.annotation.Single

@Single
class VoiceSettingRepositoryImpl(
    private val pref: VoiceSettingPreferences
) : VoiceSettingRepository {

    override fun save(value: VoiceSetting) {
        pref.save(value.toShared())
    }

    override fun read(): VoiceSetting? {
        return pref.read()?.toDomain()
    }

}