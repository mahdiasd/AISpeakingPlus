package ir.speaking.feature.tts.routing

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.feature.tts.model.SynthesizeRequest
import ir.speaking.feature.tts.model.SynthesizeResponse
import ir.speaking.feature.tts.service.TtsService
import org.koin.ktor.ext.inject

fun Application.ttsRouting() {
    val ttsService by inject<TtsService>()

    routing {
        route("/api/v1/tts") {
            // 1. List available voices
            get("/voices") {
                val voices = ttsService.getVoices()
                call.successRespond(voices)
            }

            // 2. Synthesize text to audio metadata & URL
            post("/synthesize") {
                val request = call.receive<SynthesizeRequest>()
                if (request.text.isBlank()) {
                    call.failureRespond(HttpStatusCode.BadRequest, "Text cannot be blank")
                    return@post
                }

                val voiceId = request.voiceId ?: 0
                val speed = request.speed ?: 1.0f

                val result = ttsService.synthesize(
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

            // 3. Direct audio streaming endpoint for MediaPlayer / AudioPlayer
            get("/speak") {
                val text = call.request.queryParameters["text"]?.trim()
                if (text.isNullOrBlank()) {
                    call.respond(HttpStatusCode.BadRequest, "Missing 'text' query parameter")
                    return@get
                }

                val voiceId = call.request.queryParameters["voiceId"]?.toIntOrNull() ?: 0
                val speed = call.request.queryParameters["speed"]?.toFloatOrNull() ?: 1.0f

                val result = ttsService.synthesize(text = text, sid = voiceId, speed = speed)
                if (result != null) {
                    val file = ttsService.getAudioFile(result.filename)
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

            // 4. Serve cached audio file by filename
            get("/audio/{filename}") {
                val filename = call.parameters["filename"]
                if (filename.isNullOrBlank() || !filename.endsWith(".wav")) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid audio filename")
                    return@get
                }

                val file = ttsService.getAudioFile(filename)
                if (file != null && file.exists()) {
                    call.response.header(HttpHeaders.ContentType, "audio/wav")
                    call.response.header(HttpHeaders.CacheControl, "public, max-age=31536000, immutable")
                    call.respondFile(file)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Audio file not found")
                }
            }
        }
    }
}
