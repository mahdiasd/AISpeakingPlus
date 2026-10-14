package ir.aispeaking.utils

import co.touchlab.kermit.Logger
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json

// Converts vararg elements into an ImmutableList
fun <T> immutableListOf(vararg elements: T): ImmutableList<T> = elements.toList().toImmutableList()

// Multiplatform logging using Kermit
fun Any?.dLog(plusTag: String = "", tag: String = "MyLog") {
    // Kermit automatically routes logs to the correct platform console
    // (e.g., Logcat for Android, NSLog for iOS) without manual initialization.
    Logger.withTag("$tag $plusTag").d { this.toString() }
}

val jsonForConvert = Json { ignoreUnknownKeys = true }

// JSON serialization extensions
inline fun <reified T> T?.toJson(): String? {
    return if (this == null) null
    else jsonForConvert.encodeToString(this)
}

// JSON deserialization extensions
inline fun <reified T> String?.fromJson(): T? {
    return if (this.isNullOrEmpty()) null
    else jsonForConvert.decodeFromString(this)
}