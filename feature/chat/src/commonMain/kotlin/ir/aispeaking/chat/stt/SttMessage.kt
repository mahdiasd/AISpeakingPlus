package ir.aispeaking.chat.stt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wire contract for the server-side streaming STT endpoint at `/api/v1/stt`.
 *
 * The backend runs sherpa-onnx (English, streaming-zipformer-en, int8). It
 * accepts raw mono 16 kHz PCM16 little-endian audio as binary WebSocket frames
 * and emits JSON text frames of these four shapes.
 *
 * See `STT_CLIENT_INTEGRATION.md` (project root) for the full protocol.
 */
@Serializable
sealed interface SttMessage {

    @Serializable
    @SerialName("ready")
    data class Ready(val message: String = "") : SttMessage

    @Serializable
    @SerialName("partial")
    data class Partial(val text: String) : SttMessage

    @Serializable
    @SerialName("final")
    data class Final(val text: String) : SttMessage

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : SttMessage
}

/**
 * Json instance that mirrors the server's STT serializer:
 *   - `classDiscriminator = "type"` — discriminator field used by the sealed type.
 *   - `explicitNulls = false`    — match server's nullability defaults.
 *   - `ignoreUnknownKeys = true` — forward-compatible if the server adds fields.
 *   - `encodeDefaults = true`    — emit default values for the `ready` `message` field.
 */
val sttJson: Json = Json {
    classDiscriminator = "type"
    explicitNulls = false
    ignoreUnknownKeys = true
    encodeDefaults = true
}
