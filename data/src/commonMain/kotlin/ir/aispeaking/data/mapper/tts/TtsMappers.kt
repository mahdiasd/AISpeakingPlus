package ir.aispeaking.data.mapper.tts

import ir.aispeaking.domain.model.tts.SynthesizeAudioResult
import ir.aispeaking.domain.model.tts.TtsVoice
import ir.aispeaking.domain.model.tts.VoiceAccent
import ir.aispeaking.domain.model.tts.VoiceGender
import ir.aispeaking.network.model.tts.dto.SynthesizeResponseDto
import ir.aispeaking.network.model.tts.dto.TtsVoiceDto

fun TtsVoiceDto.toDomain(): TtsVoice {
    val domainGender = when (gender.uppercase()) {
        "MALE" -> VoiceGender.MALE
        else -> VoiceGender.FEMALE
    }
    val domainAccent = when (accent.uppercase()) {
        "BRITISH" -> VoiceAccent.BRITISH
        else -> VoiceAccent.AMERICAN
    }
    return TtsVoice(
        id = id,
        code = code,
        name = name,
        gender = domainGender,
        accent = domainAccent,
        language = language,
        description = description
    )
}

fun SynthesizeResponseDto.toDomain(audioBytes: ByteArray? = null): SynthesizeAudioResult {
    return SynthesizeAudioResult(
        audioUrl = audioUrl,
        durationMs = durationMs,
        sampleRate = sampleRate,
        voiceId = voiceId,
        voiceName = voiceName,
        audioBytes = audioBytes
    )
}
