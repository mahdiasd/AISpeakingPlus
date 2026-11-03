package ir.aispeaking.utils.analytics

import co.touchlab.kermit.Logger
import org.koin.core.annotation.Single

@Single
class DefaultAnalyticsTracker : AnalyticsTracker {
    private val logger = Logger.withTag("Analytics")

    override fun logEvent(name: String, params: Map<String, Any?>) {
        logger.d { "Event: $name | Params: $params" }
        onEventLogged?.invoke(name, params)
    }

    override fun setUserId(userId: String?) {
        logger.d { "User ID: $userId" }
        onUserIdSet?.invoke(userId)
    }

    override fun setUserProperty(name: String, value: String?) {
        logger.d { "User Property: $name = $value" }
        onUserPropertySet?.invoke(name, value)
    }

    companion object {
        var onEventLogged: ((name: String, params: Map<String, Any?>) -> Unit)? = null
        var onUserIdSet: ((userId: String?) -> Unit)? = null
        var onUserPropertySet: ((name: String, value: String?) -> Unit)? = null
    }
}
