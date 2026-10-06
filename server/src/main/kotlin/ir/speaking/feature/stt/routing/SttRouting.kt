package ir.speaking.feature.stt.routing

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.server.websocket.*
import io.ktor.utils.io.*
import io.ktor.websocket.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUidOrNull
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.stt.dto.SttMessage
import ir.speaking.feature.stt.dto.SttTranscribeResponse
import ir.speaking.feature.stt.service.SttService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import java.util.UUID

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
 * Route: `/api/v2/stt/live` and `/api/v1/stt` (authenticated via user or admin JWT).
 */
@OptIn(ExperimentalKtorApi::class)
fun Application.sttRouting() {
    val sttService by inject<SttService>()

    routing {
        // v2 Route with OpenAPI describe
        route("/api/v2/stt") {
            authenticate(MyConstant.USER_JWT_NAME, MyConstant.ADMIN_JWT_NAME) {
                webSocket("/live") {
                    handleSttSession(sttService)
                }.describe {
                    tag("STT")
                    summary = "Live STT Stream"
                    description = "Bidirectional WebSocket connection for real-time streaming speech-to-text (mono 16kHz PCM16)"
                    responses {
                        HttpStatusCode.SwitchingProtocols {
                            description = "برقراری موفقیت‌آمیز اتصال وب‌سوکت استریم صوت"
                        }
                        HttpStatusCode.Unauthorized {
                            description = "توکن کاربر نامعتبر یا منقضی است"
                            schema = jsonSchema<FailureResponse>()
                        }
                        HttpStatusCode.ServiceUnavailable {
                            description = "ظرفیت پردازش همزمان صوت تکمیل است (server_busy)"
                        }
                    }
                }

                post("/transcribe") {
                    handleSttTranscribe(sttService)
                }.describe {
                    tag("STT")
                    summary = "Transcribe Audio"
                    description = "Transcribe audio file or raw PCM/WAV 16kHz to text"
                    responses {
                        HttpStatusCode.OK {
                            description = "متن پیاده‌سازی شده با موفقیت دریافت شد"
                            schema = jsonSchema<SuccessResponse<SttTranscribeResponse>>()
                        }
                        HttpStatusCode.BadRequest {
                            description = "داده صوتی نامعتبر است"
                            schema = jsonSchema<FailureResponse>()
                        }
                    }
                }
            }
        }

        // Dedicated Admin STT endpoints
        route("/api/admin/stt") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                webSocket("/live") {
                    handleSttSession(sttService)
                }
                post("/transcribe") {
                    handleSttTranscribe(sttService)
                }
            }
        }

        // v1 Legacy route preserved for backward compatibility
        route("/api/v1/stt") {
            authenticate(MyConstant.USER_JWT_NAME, MyConstant.ADMIN_JWT_NAME) {
                webSocket {
                    handleSttSession(sttService)
                }
            }
        }
    }
}

private suspend fun DefaultWebSocketServerSession.handleSttSession(sttService: SttService) {
    val userId = call.getUserUidOrNull() ?: call.principal<AdminPrincipal>()?.id ?: UUID.randomUUID()
    sttLogger.info("STT WebSocket connected from {} (user={})",
        call.request.local.remoteHost, userId)

    // Try to acquire one of the limited concurrent stream slots.
    val stream = sttService.tryAcquireStream()
    if (stream == null) {
        sttLogger.warn("STT server busy ({}/{} streams in use); rejecting connection",
            sttService.activeStreams, sttService.maxConcurrentStreams)
        sendText(SttMessage.ErrorMessage("server_busy"))
        close(CloseReason(CloseReason.Codes.TRY_AGAIN_LATER, "server_busy"))
        return
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

private suspend fun io.ktor.server.routing.RoutingContext.handleSttTranscribe(sttService: SttService) {
    val audioBytes = try {
        if (call.request.contentType().match(ContentType.MultiPart.FormData)) {
            var bytes: ByteArray? = null
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem && bytes == null) {
                    bytes = part.streamProvider().readBytes()
                }
                part.dispose()
            }
            bytes
        } else {
            call.receive<ByteArray>()
        }
    } catch (_: Exception) {
        null
    }

    if (audioBytes == null || audioBytes.isEmpty()) {
        call.failureRespond(HttpStatusCode.BadRequest, "داده‌های صوتی دریافت نشد")
        return
    }

    val samples = audioBytes.extractPcm16Samples()
    if (samples.isEmpty()) {
        call.failureRespond(HttpStatusCode.BadRequest, "قالب فایل صوتی نامعتبر است (صوت باید 16kHz PCM16 باشد)")
        return
    }

    val text = sttService.transcribeWaveform(samples)
    if (text == null) {
        call.failureRespond(HttpStatusCode.ServiceUnavailable, "موتور پردازش گفتار در دسترس نیست یا ظرفیت آن تکمیل است")
        return
    }

    val durationMs = (samples.size * 1000L) / 16000L
    call.successRespond(
        SttTranscribeResponse(
            text = text,
            durationMs = durationMs
        )
    )
}

/**
 * Extracts PCM16 audio samples from either a WAV container or raw PCM16 byte array.
 */
private fun ByteArray.extractPcm16Samples(): FloatArray {
    val pcmBytes = if (size >= 44 && this[0] == 'R'.code.toByte() && this[1] == 'I'.code.toByte() && this[2] == 'F'.code.toByte() && this[3] == 'F'.code.toByte()) {
        var dataOffset = 12
        var foundData: ByteArray? = null
        while (dataOffset + 8 <= size) {
            val chunkId = String(this, dataOffset, 4, Charsets.US_ASCII)
            val chunkSize = (this[dataOffset + 4].toInt() and 0xFF) or
                    ((this[dataOffset + 5].toInt() and 0xFF) shl 8) or
                    ((this[dataOffset + 6].toInt() and 0xFF) shl 16) or
                    ((this[dataOffset + 7].toInt() and 0xFF) shl 24)
            if (chunkId == "data") {
                val start = dataOffset + 8
                val end = (start + chunkSize).coerceAtMost(size)
                foundData = this.copyOfRange(start, end)
                break
            }
            dataOffset += 8 + chunkSize
        }
        foundData ?: this.copyOfRange(44.coerceAtMost(size), size)
    } else {
        this
    }

    val sampleCount = pcmBytes.size ushr 1
    if (sampleCount == 0) return FloatArray(0)
    val out = FloatArray(sampleCount)
    var j = 0
    var i = 0
    while (i + 1 < pcmBytes.size) {
        val lo = pcmBytes[i].toInt() and 0xFF
        val hi = pcmBytes[i + 1].toInt()
        val sample = (lo or (hi shl 8)).toShort().toInt()
        out[j] = sample / 32768.0f
        i += 2
        j++
    }
    return out
}

