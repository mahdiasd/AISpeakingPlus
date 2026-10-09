package ir.aispeaking.network

// Root-relative base so URLs become "/v1/config" etc.
// These are served by the webpack dev-server proxy which forwards
// them to the real API server, avoiding CORS during local development.
private fun jsGetWindowOrigin(): String {
    return js("typeof window !== 'undefined' && window.location ? window.location.origin : ''") as String
}

actual fun platformBaseUrl(): String {
    val origin = try {
        jsGetWindowOrigin()
    } catch (_: Throwable) {
        ""
    }
    return origin.ifBlank { BuildConfig.BaseUrl }
}
