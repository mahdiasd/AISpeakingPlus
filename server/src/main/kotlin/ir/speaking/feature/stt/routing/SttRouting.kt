package ir.speaking.feature.stt.routing

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.feature.stt.dto.SttMessage
import ir.speaking.feature.stt.service.SttService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory

private val sttLogger = LoggerFactory.getLogger("ir.speaking.feature.stt")
private val sttJson = Json {
    encodeDefaults = true
    explicitNulls = false
    classDiscriminator = "type"
}
private const val ARG_MAX_FRAME_SAMPLES = 16_000 // 1s @ 16 kHz — generous upper bound for a single binary frame

/**
 * WebSocket endpoint for streaming English speech-to-text.
 *
 * Route: `/api/v1/stt` (authenticated via user JWT).
 *
 * The `/api/` prefix is proxied by nginx (with WebSocket upgrade headers +
 * 24 h read timeout), so this route is reachable through the reverse proxy
 * without an extra nginx block.
 *
 * Protocol:
 *  - The client streams **mono 16 kHz PCM16** audio as binary frames
 *    (chunks of roughly 100–300 ms, i.e. 3,200–9,600 samples per frame).
 *  - The server sends [SttMessage] instances as text frames (JSON):
 *      * {"type":"ready","message":"..."}        — once, after the stream is acquired
 *      * {"type":"partial","text":"..."}          — interim result after each decode step
 *      * {"type":"final","text":"..."}            — result after an endpoint (utterance boundary)
 *      * {"type":"error","message":"server_busy"}— capacity reached; connection is closed
 *
 * Resource safety: the [OnlineStream] is always released in a `finally`
 * block so that native memory is freed even if the client disconnects
 * abruptly (TCP reset, crash, or WebSocket timeout).
 */
fun Application.sttRouting() {
    val sttService by inject<SttService>()

    routing {
        route("/api/v1/stt") {
            authenticate(MyConstant.USER_JWT_NAME) {
                webSocket {
                    val userId = call.getUserUid()
                    sttLogger.info("STT WebSocket connected from {} (user={})",
                        call.request.local.remoteHost, userId)

                    // Try to acquire one of the limited concurrent stream slots.
                    val stream = sttService.tryAcquireStream()
                    if (stream == null) {
                        sttLogger.warn("STT server busy ({}/{} streams in use); rejecting connection",
                            sttService.activeStreams, sttService.maxConcurrentStreams)
                        sendText(SttMessage.ErrorMessage("server_busy"))
                        close(CloseReason(CloseReason.Codes.TRY_AGAIN_LATER, "server_busy"))
                        return@webSocket
                    }

                    sttLogger.info("STT stream acquired ({}/{} active)",
                        sttService.activeStreams, sttService.maxConcurrentStreams)

                    try {
                        // Signal readiness so the client knows it can start sending audio.
                        sendText(SttMessage.ReadyMessage())

                        var lastPartialText = ""

                        for (frame in incoming) {
                            when (frame) {
                                is Frame.Binary -> handleBinaryFrame(sttService, stream, frame,
                                    send = { msg -> sendText(msg) },
                                    onPartial = { newText ->
                                        if (newText != lastPartialText) {
                                            lastPartialText = newText
                                        }
                                    },
                                )
                                is Frame.Text -> {
                                    // A text frame could be used as a control message in the future
                                    // (e.g. "stop"). For now we simply ignore it.
                                }
                                is Frame.Close -> {
                                    sttLogger.info("STT client closed the connection")
                                    break
                                }
                                else -> { /* Ping/Pong are handled by the WebSocket plugin */ }
                            }
                        }
                    } catch (e: Throwable) {
                        sttLogger.error("STT WebSocket error from {}", call.request.local.remoteHost, e)
                    } finally {
                        sttService.releaseStream(stream)
                        sttLogger.info("STT stream released ({}/{} active)",
                            sttService.activeStreams, sttService.maxConcurrentStreams)
                    }
                }
            }
        }
    }
}

/**
 * Accepts a single binary audio frame, feeds it into the [OnlineStream], and
 * runs a decode step. When an endpoint is detected the accumulated text is
 * sent as a [SttMessage.FinalTranscript]; otherwise the current interim text
 * is sent as a [SttMessage.PartialTranscript] (only when it changes).
 */
private suspend fun handleBinaryFrame(
    sttService: SttService,
    stream: com.k2fsa.sherpa.onnx.OnlineStream,
    frame: Frame.Binary,
    send: suspend (SttMessage) -> Unit,
    onPartial: (String) -> Unit,
) {
    val samples = frame.data.toShortArraySamples()
    if (samples.isEmpty()) return
    sttService.acceptWaveform(stream, samples)

    // Decode as far as the model is ready; then read the current hypothesis.
    sttService.decodeIfReady(stream)
    val text = sttService.getText(stream)

    if (sttService.isEndpoint(stream)) {
        // Endpoint fired → emit the final transcript for this utterance,
        // then reset the stream to start a fresh utterance.
        if (text.isNotBlank()) {
            send(SttMessage.FinalTranscript(text))
            onPartial("")
        }
        sttService.reset(stream)
    } else if (text.isNotBlank()) {
        send(SttMessage.PartialTranscript(text))
        onPartial(text)
    }
}

/**
 * Converts the raw payload of a binary frame (little-endian signed 16-bit PCM)
 * into the normalized [-1.0, 1.0] float array expected by sherpa-onnx.
 */
private fun ByteArray.toShortArraySamples(): FloatArray {
    // Each sample is 2 bytes, little-endian, signed.
    val sampleCount = size ushr 1
    if (sampleCount == 0) return FloatArray(0)
    if (sampleCount > ARG_MAX_FRAME_SAMPLES) {
        // Guard against absurdly large frames that could exhaust memory.
        return FloatArray(0)
    }

    val out = FloatArray(sampleCount)
    var j = 0
    var i = 0
    while (i + 1 < size) {
        // Little-endian signed 16-bit → float
        val lo = this[i].toInt() and 0xFF
        val hi = this[i + 1].toInt()
        val sample = (lo or (hi shl 8)).toShort().toInt()
        out[j] = sample / 32768.0f
        i += 2
        j++
    }
    return out
}

private suspend fun DefaultWebSocketSession.sendText(msg: SttMessage) {
    send(sttJson.encodeToString(msg))
}
