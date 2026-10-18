package ir.aispeaking.utils.analytics

import co.touchlab.kermit.Logger
import org.koin.core.annotation.Single

@Single
class DefaultAnalyticsTracker : AnalyticsTracker {
    private val logger = Logger.withTag("Analytics")

    override fun logEvent(name: String, params: Map<String, Any?>) {
        logger.d { "Event: $name | Params: $params" }
    }

    override fun setUserId(userId: String?) {
        logger.d { "User ID: $userId" }
    }

    override fun setUserProperty(name: String, value: String?) {
        logger.d { "User Property: $name = $value" }
    }
}
