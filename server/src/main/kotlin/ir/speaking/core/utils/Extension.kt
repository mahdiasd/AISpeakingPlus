package ir.speaking.core.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import ir.speaking.core.exeptions.AppException
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*


@OptIn(ExperimentalSerializationApi::class)
val jsonForConvert = Json {
    ignoreUnknownKeys = true
    isLenient = true
    allowTrailingComma = true
    encodeDefaults = true
    explicitNulls = false
}

inline fun <reified T> T?.toJson(): String? {
    return if (this == null) return null
    else jsonForConvert.encodeToString(this)
}

inline fun <reified T> T?.toJsonElement(): JsonElement? {
    return if (this == null) return null
    else jsonForConvert.encodeToJsonElement(this)
}

inline fun <reified T> String?.fromJson(): T? {
    return if (this.isNullOrEmpty()) return null
    else jsonForConvert.decodeFromString(this)
}

fun String?.toUUID(exceptionMessage: String = "invalid uuid"): UUID {
    when {
        this.isNullOrEmpty() -> throw AppException.BadRequest(exceptionMessage)
        else -> return try {
            UUID.fromString(this)
        } catch (e: Exception) {
            throw AppException.BadRequest(exceptionMessage)
        }
    }
}

fun ApplicationCall.getBaseUrl(): String {
    val proto = request.headers["X-Forwarded-Proto"]
        ?.takeIf { it.isNotBlank() }
        ?: request.origin.scheme

    val host = request.headers["X-Forwarded-Host"]
        ?.takeIf { it.isNotBlank() }
        ?: request.headers[HttpHeaders.Host]
        ?.takeIf { it.isNotBlank() }
        ?: run {
            val h = request.origin.serverHost
            val p = request.origin.serverPort
            if (p == 80 || p == 443 || p <= 0) h else "$h:$p"
        }

    return "$proto://$host".removeSuffix("/")
}

fun ApplicationCall.getFullPath(imagePath: String?): String? {
    if (imagePath.isNullOrBlank()) return null
    if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
        return imagePath
    }
    val cleanPath = imagePath.trim().removePrefix("/")
    return "${getBaseUrl()}/resources/$cleanPath"
}

fun ApplicationCall.getUserUid(): UUID {
    try {
        return this.principal<JWTPrincipal>()!!.payload.getClaim("uid").asString().toUUID()
    } catch (ex: Exception) {
        throw AppException.UnauthorizedAccess()
    }
}

fun ApplicationCall.getUserUidOrNull(): UUID? {
    return try {
        this.principal<JWTPrincipal>()!!.payload.getClaim("uid").asString().toUUID()
    } catch (ex: Exception) {
        null
    }
}