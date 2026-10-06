package ir.speaking.feature.stt.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * JSON messages sent from the server to the WebSocket client over /ws/stt.
 *
 *  - [PartialTranscript] : interim recognition result (updated as more audio arrives)
 *  - [FinalTranscript]   : result after an endpoint (silence / end-of-utterance) is detected
 *  - [ErrorMessage]      : protocol or capacity errors (e.g. "server_busy")
 *  - [ReadyMessage]      : sent once immediately after a successful stream acquisition
 */
@Serializable
sealed interface SttMessage {

    @Serializable
    @SerialName("partial")
    data class PartialTranscript(
        val text: String
    ) : SttMessage

    @Serializable
    @SerialName("final")
    data class FinalTranscript(
        val text: String
    ) : SttMessage

    @Serializable
    @SerialName("error")
    data class ErrorMessage(
        val message: String
    ) : SttMessage

    @Serializable
    @SerialName("ready")
    data class ReadyMessage(
        val message: String = "Stream ready. Send mono PCM16 audio as binary frames."
    ) : SttMessage
}

@Serializable
data class SttTranscribeResponse(
    val text: String,
    val durationMs: Long
)

