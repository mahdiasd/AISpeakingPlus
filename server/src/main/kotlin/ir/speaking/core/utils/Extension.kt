package ir.speaking.core.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.*
import ir.speaking.core.exeptions.AppException
import ir.speaking.feature.challenge.challenge.model.Challenge
import ir.speaking.feature.scenario.scenario.model.Scenario
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


@OptIn(ExperimentalSerializationApi::class)
val jsonForConvert = Json {
    ignoreUnknownKeys = true
    isLenient = true
    allowTrailingComma = true
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

/**
 * Extension function to convert an Instant to a human-readable string.
 *
 * @param zoneId The time zone to use for conversion (defaults to system's default).
 * @return A string like "August 20, 2025 at 3:45 PM".
 */
fun Instant.toHumanReadable(zoneId: ZoneId = ZoneId.systemDefault()): String {
    val zonedDateTime: ZonedDateTime = this.atZone(zoneId)
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")
    return zonedDateTime.format(formatter)
}

fun String.toUUIDOrNull(): UUID? {
    return try {
        UUID.fromString(this)
    } catch (e: Exception) {
        null
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

fun Scenario.getPrompt(): String {
    val tasksFormatted = this.tasks.mapIndexed { index, task ->
        "${index}. ${task.description}"
    }.joinToString(separator = "\n")

    val prompt = MyConstant.BASE_PROMPT
        .replace("[AI_NAME]", this.aiName.ifEmpty { "Assistant" })
        .replace("[AI_ROLE]", this.aiRole.ifEmpty { "Conversation Partner" })
        .replace("[SCENARIO_DESCRIPTION]", this.description)
        .replace("[STARTER_ROLE]", this.starter.name)
        .replace("[USER_TASKS]", tasksFormatted.ifEmpty { "No tasks defined" })
        .trimIndent()

    logInfo("Generated System Prompt:\n$prompt")
    return prompt
}


fun Challenge.getPrompt(): String {
    val tasksFormatted = this.tasks.mapIndexed { index, task ->
        "${index}. ${task.description}"
    }.joinToString(separator = "\n")

    val prompt = MyConstant.BASE_PROMPT
        .replace("[AI_NAME]", this.aiName.ifEmpty { "Assistant" })
        .replace("[AI_ROLE]", this.aiRole.ifEmpty { "Conversation Partner" })
        .replace("[SCENARIO_DESCRIPTION]", this.description)
        .replace("[STARTER_ROLE]", this.starter.name)
        .replace("[USER_TASKS]", tasksFormatted.ifEmpty { "No tasks defined" })
        .trimIndent()


    logInfo("Generated System Prompt:\n$prompt")
    return prompt
}


fun LocalDateTime.Companion.now(): LocalDateTime {
    return java.time.LocalDateTime.now().toKotlinLocalDateTime()
}

fun LocalDate.Companion.of(year: Int, month: Int, day: Int): LocalDate {
    return java.time.LocalDate.of(year, month, day).toKotlinLocalDate()
}

fun String.toLocalDateTime(): LocalDateTime {
    return java.time.LocalDateTime.parse(
        this,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    ).toKotlinLocalDateTime()
}

fun String.toLocalDate(): LocalDate {
    return java.time.LocalDate.parse(
        this,
        DateTimeFormatter.ofPattern("yyyy-MM-dd")
    ).toKotlinLocalDate()
}