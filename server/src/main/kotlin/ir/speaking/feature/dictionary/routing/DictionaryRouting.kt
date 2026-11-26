package ir.speaking.feature.dictionary.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.dictionary.dto.DictionaryLookupRequest
import ir.speaking.feature.dictionary.dto.DictionaryLookupResponse
import ir.speaking.feature.dictionary.service.DictionaryService
import ir.speaking.feature.dictionary.validation.InvalidWordException
import org.koin.ktor.ext.inject
import java.io.File

@OptIn(ExperimentalKtorApi::class)
fun Application.dictionaryRouting() {
    routing {
        // v2 Primary route
        route("/api/v2/dictionary") {
            registerDictionaryEndpoints(tag = "Dictionary", suffix = "")
            get("/audio/{filename}") {
                serveWordAudio(call)
            }
            head("/audio/{filename}") {
                serveWordAudioHead(call)
            }
        }

        // v1 / Root API route for direct compatibility
        route("/api/dictionary") {
            registerDictionaryEndpoints(tag = "Dictionary", suffix = " (Legacy)")
            get("/audio/{filename}") {
                serveWordAudio(call)
            }
            head("/audio/{filename}") {
                serveWordAudioHead(call)
            }
        }

        // Word audio file static serving
        get("/audio_cache/words/{filename}") {
            serveWordAudio(call)
        }.describe {
            tag("Dictionary")
            summary = "Get Word Audio File"
            description = "Download or stream cached pronunciation audio file for dictionary lemma"
            parameters {
                path("filename") {
                    description = "Audio filename ending with .mp3 or .wav"
                    required = true
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "Audio file stream"
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

        head("/audio_cache/words/{filename}") {
            serveWordAudioHead(call)
        }
    }
}

private fun resolveAudioFile(filename: String?): File? {
    if (filename.isNullOrBlank()) return null
    val sanitized = File(filename).name
    val candidates = listOf(
        File("audio_cache/words", sanitized),
        File("server/audio_cache/words", sanitized),
        File("../audio_cache/words", sanitized),
        File("/app/audio_cache/words", sanitized),
        File("audio_cache", sanitized),
        File("/app/audio_cache", sanitized)
    )
    return candidates.firstOrNull { it.exists() && it.isFile }
}

private suspend fun serveWordAudio(call: ApplicationCall) {
    val filename = call.parameters["filename"]
    val file = resolveAudioFile(filename)
    if (file != null && file.exists()) {
        val sanitized = File(filename!!).name
        val contentType = if (sanitized.endsWith(".mp3", ignoreCase = true)) "audio/mpeg" else "audio/wav"
        call.response.header(HttpHeaders.ContentType, contentType)
        call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"$sanitized\"")
        call.response.header(HttpHeaders.CacheControl, "public, max-age=31536000, immutable")
        call.response.header(HttpHeaders.AcceptRanges, "bytes")
        call.respondFile(file)
    } else {
        call.failureRespond(HttpStatusCode.NotFound, "Audio file not found")
    }
}

private suspend fun serveWordAudioHead(call: ApplicationCall) {
    val filename = call.parameters["filename"]
    val file = resolveAudioFile(filename)
    if (file != null && file.exists()) {
        val sanitized = File(filename!!).name
        val contentType = if (sanitized.endsWith(".mp3", ignoreCase = true)) "audio/mpeg" else "audio/wav"
        call.response.header(HttpHeaders.ContentType, contentType)
        call.response.header(HttpHeaders.ContentLength, file.length().toString())
        call.response.header(HttpHeaders.ContentDisposition, "inline; filename=\"$sanitized\"")
        call.response.header(HttpHeaders.CacheControl, "public, max-age=31536000, immutable")
        call.response.header(HttpHeaders.AcceptRanges, "bytes")
        call.respond(HttpStatusCode.OK)
    } else {
        call.respond(HttpStatusCode.NotFound)
    }
}

@OptIn(ExperimentalKtorApi::class)
private fun Route.registerDictionaryEndpoints(
    tag: String,
    suffix: String
) {
    val dictionaryService by inject<DictionaryService>()
    authenticate(MyConstant.USER_JWT_NAME, optional = true) {
        post("/lookup") {
            val request = try {
                call.receive<DictionaryLookupRequest>()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "Invalid request body: ${e.message}")
                return@post
            }

            if (request.word.isBlank()) {
                call.failureRespond(HttpStatusCode.BadRequest, "Word cannot be empty")
                return@post
            }

            try {
                val response = dictionaryService.lookup(request.word, request.contextSentence)
                call.successRespond(response, message = "Word looked up successfully")
            } catch (e: InvalidWordException) {
                call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "عبارت واردشده معتبر نیست.")
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.InternalServerError, "Failed to lookup word: ${e.message}")
            }
        }.describe {
            this.tag(tag)
            summary = "Lookup Word$suffix"
            description = "Lookup word lemma, phonetic, POS, contextual Persian meaning, other meanings, and pronunciation"
            requestBody {
                description = "Lookup request containing word and optional context sentence"
                required = true
                schema = jsonSchema<DictionaryLookupRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "Lookup result retrieved successfully"
                    schema = jsonSchema<SuccessResponse<DictionaryLookupResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "Invalid, nonsensical, non-English, or inappropriate word or request body"
                    schema = jsonSchema<FailureResponse>()
                }
                HttpStatusCode.InternalServerError {
                    description = "Lookup failed"
                    schema = jsonSchema<FailureResponse>()
                }
            }
        }
    }
}
