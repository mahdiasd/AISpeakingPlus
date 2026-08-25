package ir.aispeaking.utils

import co.touchlab.kermit.Logger
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.json.Json

// Returns an empty ImmutableList
fun <T> immutableListOf(): ImmutableList<T> = emptyList<T>().toImmutableList()

// Converts vararg elements into an ImmutableList
fun <T> immutableListOf(vararg elements: T): ImmutableList<T> = elements.toList().toImmutableList()

// Converts String to Int safely without using Java's Integer.parseInt
fun String.safeToInt(): Int {
    if (this.isEmpty()) return 0
    val clearedInt = this
        .replace(".0", "")
        .replace(" ", "")
        .replace(".", "")

    // toIntOrNull() is a Kotlin Standard Library function, fully KMP compatible
    return clearedInt.toIntOrNull() ?: 0
}

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

// Adds spaces to a string in chunks
fun String.addSpaces(sliceSize: Int = 3): String {
    return chunked(sliceSize).joinToString(" ")
}