package ir.aispeaking.domain.usecase.voice_setting

import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.domain.repository.voice_setting.VoiceSettingRepository
import org.koin.core.annotation.Single

@Single
class SaveVoiceSettingUseCase(private val repo: VoiceSettingRepository) {
    suspend operator fun invoke(value: VoiceSetting) = repo.save(value)
}