package ir.speaking.feature.tts.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.feature.tts.model.SynthesizeRequest
import ir.speaking.feature.tts.model.SynthesizeResponse
import ir.speaking.feature.tts.model.TtsVoiceInfo
import ir.speaking.feature.tts.service.TtsService
import org.koin.ktor.ext.inject

@OptIn(ExperimentalKtorApi::class)
fun Application.ttsRouting() {
    val ttsServiceLazy = inject<TtsService>()

    routing {
        // v2 Primary route with OpenAPI documentation
        route("/api/v2/tts") {
            registerTtsEndpoints(ttsService = { ttsServiceLazy.value }, tag = "TTS", suffix = "")
        }

        // v1 Legacy route preserved for backward compatibility
        route("/api/v1/tts") {
            registerTtsEndpoints(ttsService = { ttsServiceLazy.value }, tag = "Legacy v1", suffix = " (v1)")
        }
    }
}

@OptIn(ExperimentalKtorApi::class)
private fun Route.registerTtsEndpoints(ttsService: () -> TtsService, tag: String, suffix: String) {
    // 1. List available voices
    val voicesRoute = get("/voices") {
        val voices = ttsService().getVoices()
        call.successRespond(voices)
    }
    voicesRoute.describe {
        tag(tag)
        summary = "List Voices$suffix"
        description = "Get list of available Kokoro TTS voices with gender, accent, and id"
        responses {
            HttpStatusCode.OK {
                description = "List of TTS voices retrieved successfully"
                schema = jsonSchema<SuccessResponse<List<TtsVoiceInfo>>>()
            }
            HttpStatusCode.InternalServerError {
                description = "Server error"
                schema = jsonSchema<FailureResponse>()
            }
        }
    }

    // 2. Synthesize text to audio metadata & URL
    val synthesizeRoute = post("/synthesize") {
        val request = call.receive<SynthesizeRequest>()
        if (request.text.isBlank()) {
            call.failureRespond(HttpStatusCode.BadRequest, "Text cannot be blank")
            return@post
        }

        val voiceId = request.voiceId ?: 0
        val speed = request.speed ?: 1.0f

        val result = ttsService().synthesize(
            text = request.text,
            sid = voiceId,
            speed = speed
        )

        if (result != null) {
            call.successRespond(
                SynthesizeResponse(
                    audioUrl = result.audioUrl,
                    durationMs = result.durationMs,
                    sampleRate = result.sampleRate,
                    voiceId = result.voiceId,
                    voiceName = result.voiceName
                )
            )
        } else {
            call.failureRespond(
                HttpStatusCode.InternalServerError,
                "TTS engine is currently unavailable or failed to synthesize audio"
            )
        }
    }
    synthesizeRoute.describe {
        tag(tag)
        summary = "Synthesize Speech$suffix"
        description = "Synthesize English text to speech audio URL with duration and voice info"
        requestBody {
            description = "Synthesis payload including text, optional voiceId, and speed"
            required = true
            schema = jsonSchema<SynthesizeRequest>()
        }
        responses {
            HttpStatusCode.OK {
                description = "Audio synthesized successfully"
                schema = jsonSchema<SuccessResponse<SynthesizeResponse>>()
            }
            HttpStatusCode.BadRequest {
                description = "Invalid request payload"
                schema = jsonSchema<FailureResponse>()
            }
            HttpStatusCode.InternalServerError {
                description = "TTS engine error"
                schema = jsonSchema<FailureResponse>()
            }
        }
    }

    // 3. Direct audio streaming endpoint for MediaPlayer / AudioPlayer
    val speakRoute = get("/speak") {
        val text = call.request.queryParameters["text"]?.trim()
        if (text.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest, "Missing 'text' query parameter")
            return@get
        }

        val voiceId = call.request.queryParameters["voiceId"]?.toIntOrNull() ?: 0
        val speed = call.request.queryParameters["speed"]?.toFloatOrNull() ?: 1.0f

        val result = ttsService().synthesize(text = text, sid = voiceId, speed = speed)
        if (result != null) {
            val file = ttsService().getAudioFile(result.filename)
            if (file != null && file.exists()) {
                call.response.header(HttpHeaders.ContentType, "audio/wav")
                call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"${result.filename}\"")
                call.response.header(HttpHeaders.CacheControl, "public, max-age=86400")
                call.respondFile(file)
            } else if (result.audioData != null) {
                call.response.header(HttpHeaders.ContentType, "audio/wav")
                call.respondBytes(result.audioData, ContentType.parse("audio/wav"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Audio generation failed")
            }
        } else {
            call.respond(HttpStatusCode.InternalServerError, "TTS engine unavailable")
        }
    }

    head("/speak") {
        call.response.header(HttpHeaders.ContentType, "audio/wav")
        call.response.header(HttpHeaders.AcceptRanges, "none")
        call.respond(HttpStatusCode.OK)
    }

    speakRoute.describe {
        tag(tag)
        summary = "Stream Speech Audio$suffix"
        description = "Directly synthesize and stream WAV audio bytes for instant media playback"
        parameters {
            query("text") {
                description = "English text to synthesize"
                required = true
            }
            query("voiceId") {
                description = "Kokoro voice ID (default 0)"
                required = false
            }
            query("speed") {
                description = "Playback speed (default 1.0)"
                required = false
            }
        }
        responses {
            HttpStatusCode.OK {
                description = "Audio stream (audio/wav)"
            }
            HttpStatusCode.BadRequest {
                description = "Missing text parameter"
                schema = jsonSchema<FailureResponse>()
            }
            HttpStatusCode.InternalServerError {
                description = "TTS engine error"
                schema = jsonSchema<FailureResponse>()
            }
        }
    }

    // 4. Serve cached audio file by filename
    val audioRoute = get("/audio/{filename}") {
        val filename = call.parameters["filename"]
        if (filename.isNullOrBlank() || !filename.endsWith(".wav")) {
            call.respond(HttpStatusCode.BadRequest, "Invalid audio filename")
            return@get
        }

        val file = ttsService().getAudioFile(filename)
        if (file != null && file.exists()) {
            call.response.header(HttpHeaders.ContentType, "audio/wav")
            call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"$filename\"")
            call.response.header(HttpHeaders.CacheControl, "public, max-age=31536000, immutable")
            call.respondFile(file)
        } else {
            call.respond(HttpStatusCode.NotFound, "Audio file not found")
        }
    }

    head("/audio/{filename}") {
        val filename = call.parameters["filename"]
        if (filename.isNullOrBlank() || !filename.endsWith(".wav")) {
            call.respond(HttpStatusCode.BadRequest)
            return@head
        }
        val file = ttsService().getAudioFile(filename)
        if (file != null && file.exists()) {
            call.response.header(HttpHeaders.ContentType, "audio/wav")
            call.response.header(HttpHeaders.ContentLength, file.length().toString())
            call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"$filename\"")
            call.response.header(HttpHeaders.CacheControl, "public, max-age=31536000, immutable")
            call.response.header(HttpHeaders.AcceptRanges, "bytes")
            call.respond(HttpStatusCode.OK)
        } else {
            call.respond(HttpStatusCode.NotFound)
        }
    }

    audioRoute.describe {
        tag(tag)
        summary = "Download Audio File$suffix"
        description = "Download cached synthesized audio file by filename (WAV format)"
        parameters {
            path("filename") {
                description = "Audio filename ending with .wav"
                required = true
            }
        }
        responses {
            HttpStatusCode.OK {
                description = "Cached audio file (audio/wav)"
            }
            HttpStatusCode.BadRequest {
                description = "Invalid filename"
                schema = jsonSchema<FailureResponse>()
            }
            HttpStatusCode.NotFound {
                description = "Audio file not found"
                schema = jsonSchema<FailureResponse>()
            }
        }
    }
}
