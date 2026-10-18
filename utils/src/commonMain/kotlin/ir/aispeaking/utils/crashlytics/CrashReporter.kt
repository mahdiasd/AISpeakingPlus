package ir.aispeaking.utils.crashlytics

/**
 * Multiplatform crash and non-fatal exception reporting abstraction.
 * On Android, this routes directly to Firebase Crashlytics.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable, message: String? = null)
    fun log(message: String)
    fun setCustomKey(key: String, value: String)
    fun setCustomKey(key: String, value: Boolean)
    fun setCustomKey(key: String, value: Int)
    fun setUserId(userId: String)
}
