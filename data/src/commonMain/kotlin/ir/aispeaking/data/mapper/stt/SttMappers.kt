package ir.aispeaking.data.mapper.stt

import ir.aispeaking.domain.model.stt.SttStreamEvent
import ir.aispeaking.network.model.stt.dto.SttMessageDto

fun SttMessageDto.toDomain(): SttStreamEvent {
    return when (this) {
        is SttMessageDto.Ready -> SttStreamEvent.Ready(message = message)
        is SttMessageDto.Partial -> SttStreamEvent.PartialTranscript(text = text)
        is SttMessageDto.Final -> SttStreamEvent.FinalTranscript(text = text)
        is SttMessageDto.Error -> SttStreamEvent.Error(message = message)
    }
}
