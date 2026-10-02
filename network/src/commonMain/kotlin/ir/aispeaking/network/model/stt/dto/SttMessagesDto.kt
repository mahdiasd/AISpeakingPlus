package ir.aispeaking.network.model.stt.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface SttMessageDto {
    @Serializable
    @SerialName("ready")
    data class Ready(
        val message: String = "Stream ready"
    ) : SttMessageDto

    @Serializable
    @SerialName("partial")
    data class Partial(
        val text: String
    ) : SttMessageDto

    @Serializable
    @SerialName("final")
    data class Final(
        val text: String
    ) : SttMessageDto

    @Serializable
    @SerialName("error")
    data class Error(
        val message: String
    ) : SttMessageDto
}
