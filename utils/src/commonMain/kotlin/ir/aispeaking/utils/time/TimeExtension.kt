package ir.aispeaking.utils.time

import ir.aispeaking.utils.dLog
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun String?.toInstant(): Instant {
    val defaultInstant = Instant.parse("2000-01-01T00:00:00Z")
    if (this.isNullOrEmpty()) return defaultInstant
    return try {
        Instant.parse(this)
    } catch (e: Exception) {
//        e.message.dLog("toInstant")
        defaultInstant
    }
}

fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrEmpty()) return null
    return try {
        Instant.parse(this)
    } catch (e: Exception) {
        e.message.dLog("toInstantOrNull")
        null
    }
}

fun Instant.toReadableString(): String {
    val tehranZone = TimeZone.of("Asia/Tehran")
    val localDateTime = this.toLocalDateTime(tehranZone)
    
    val year = localDateTime.year
    val month = localDateTime.month.number.toString().padStart(2, '0')
    val day = localDateTime.day.toString().padStart(2, '0')
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    
    return "$year-$month-$day $hour:$minute"
}
