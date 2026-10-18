package ir.aispeaking.utils.analytics

/**
 * Multiplatform analytics abstraction for AISpeakingPlus.
 * Android maps this directly to Firebase Analytics, while desktop and web
 * route events to platform logs or dedicated providers without breaking CMP builds.
 */
interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserId(userId: String?)
    fun setUserProperty(name: String, value: String?)
}
