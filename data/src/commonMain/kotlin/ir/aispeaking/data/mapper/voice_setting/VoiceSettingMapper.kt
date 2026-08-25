package ir.aispeaking.data.mapper.voice_setting

import ir.aispeaking.domain.model.voice_setting.VoiceSetting
import ir.aispeaking.storage.model.voice_setting.SharedVoiceSetting


fun VoiceSetting.toShared(): SharedVoiceSetting {
    return SharedVoiceSetting(voiceId = voiceId, pitch = pitch, rate = speed, showTranscribe = showTranscribe, showSuggests = showSuggests)
}

fun SharedVoiceSetting.toDomain(): VoiceSetting {
    return VoiceSetting(voiceId = voiceId, pitch = pitch, speed = rate, showTranscribe = showTranscribe, showSuggests = showSuggests)
}