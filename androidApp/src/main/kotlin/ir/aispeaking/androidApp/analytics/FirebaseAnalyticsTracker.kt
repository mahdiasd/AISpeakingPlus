package ir.aispeaking.androidApp.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import ir.aispeaking.utils.analytics.AnalyticsTracker

class FirebaseAnalyticsTracker(context: Context) : AnalyticsTracker {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    override fun logEvent(name: String, params: Map<String, Any?>) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                when (value) {
                    null -> {}
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Float -> putFloat(key, value)
                    is Boolean -> putBoolean(key, value)
                    else -> putString(key, value.toString())
                }
            }
        }
        firebaseAnalytics.logEvent(name, bundle)
    }

    override fun setUserId(userId: String?) {
        firebaseAnalytics.setUserId(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}
