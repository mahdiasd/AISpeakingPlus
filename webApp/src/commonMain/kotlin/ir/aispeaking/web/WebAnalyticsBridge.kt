@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import ir.aispeaking.utils.analytics.DefaultAnalyticsTracker
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@JsFun("(name, paramsJson) => (window.aiSpeakingAnalytics && typeof window.aiSpeakingAnalytics.logEvent === 'function') ? window.aiSpeakingAnalytics.logEvent(name, paramsJson) : null")
private external fun jsLogAnalyticsEvent(name: String, paramsJson: String)

@JsFun("(userId) => (window.aiSpeakingAnalytics && typeof window.aiSpeakingAnalytics.setUserId === 'function') ? window.aiSpeakingAnalytics.setUserId(userId) : null")
private external fun jsSetAnalyticsUserId(userId: String)

@JsFun("(name, value) => (window.aiSpeakingAnalytics && typeof window.aiSpeakingAnalytics.setUserProperty === 'function') ? window.aiSpeakingAnalytics.setUserProperty(name, value) : null")
private external fun jsSetAnalyticsUserProperty(name: String, value: String)

fun setupWebAnalyticsBridge() {
    DefaultAnalyticsTracker.onEventLogged = { name, params ->
        try {
            val jsonMap = params.mapValues { (_, v) ->
                when (v) {
                    null -> JsonPrimitive("")
                    is Number -> JsonPrimitive(v)
                    is Boolean -> JsonPrimitive(v)
                    else -> JsonPrimitive(v.toString())
                }
            }
            val jsonString = JsonObject(jsonMap).toString()
            jsLogAnalyticsEvent(name, jsonString)
        } catch (_: Throwable) {}
    }

    DefaultAnalyticsTracker.onUserIdSet = { userId ->
        try {
            if (userId != null) {
                jsSetAnalyticsUserId(userId)
            }
        } catch (_: Throwable) {}
    }

    DefaultAnalyticsTracker.onUserPropertySet = { name, value ->
        try {
            if (value != null) {
                jsSetAnalyticsUserProperty(name, value)
            }
        } catch (_: Throwable) {}
    }
}
