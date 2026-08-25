package ir.aispeaking.domain.repository.voice_setting

import ir.aispeaking.domain.model.voice_setting.VoiceSetting

interface VoiceSettingRepository {
    fun save(value: VoiceSetting)
    fun read(): VoiceSetting?
}