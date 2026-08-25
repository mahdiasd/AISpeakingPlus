package ir.aispeaking.chat.stt

/**
 * JS browser implementation — uses the built-in `encodeURIComponent` to
 * safely embed the JWT in the `?token=` query parameter of the STT WS URL.
 */
internal actual fun encodeURIComponentCompat(value: String): String =
    js("encodeURIComponent(value)").unsafeCast<String>()
