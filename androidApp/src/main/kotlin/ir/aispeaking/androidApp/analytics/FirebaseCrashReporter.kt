package ir.aispeaking.androidApp.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics
import ir.aispeaking.utils.crashlytics.CrashReporter

class FirebaseCrashReporter : CrashReporter {
    private val crashlytics: FirebaseCrashlytics
        get() = FirebaseCrashlytics.getInstance()

    override fun recordException(throwable: Throwable, message: String?) {
        if (message != null) {
            crashlytics.log(message)
        }
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Boolean) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setCustomKey(key: String, value: Int) {
        crashlytics.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
}
