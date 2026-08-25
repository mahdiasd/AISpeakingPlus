package ir.aispeaking.chat.stt

/**
 * JVM stub — the JWT is sent in the `Authorization` header on native
 * targets, so `encodeURIComponentCompat` is never called. Returned as-is.
 */
internal actual fun encodeURIComponentCompat(value: String): String = value
