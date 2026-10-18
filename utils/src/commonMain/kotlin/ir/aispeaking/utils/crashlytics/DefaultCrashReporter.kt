package ir.aispeaking.utils.crashlytics

import co.touchlab.kermit.Logger
import org.koin.core.annotation.Single

@Single
class DefaultCrashReporter : CrashReporter {
    private val logger = Logger.withTag("CrashReporter")

    override fun recordException(throwable: Throwable, message: String?) {
        logger.e(throwable) { message ?: "Recorded exception: ${throwable.message}" }
    }

    override fun log(message: String) {
        logger.d { message }
    }

    override fun setCustomKey(key: String, value: String) {
        logger.d { "CustomKey: $key = $value" }
    }

    override fun setCustomKey(key: String, value: Boolean) {
        logger.d { "CustomKey: $key = $value" }
    }

    override fun setCustomKey(key: String, value: Int) {
        logger.d { "CustomKey: $key = $value" }
    }

    override fun setUserId(userId: String) {
        logger.d { "Crashlytics User: $userId" }
    }
}
