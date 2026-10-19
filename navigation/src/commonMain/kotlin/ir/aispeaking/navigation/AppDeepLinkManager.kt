package ir.aispeaking.navigation

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Multiplatform Deep Link and Push Notification navigation coordinator.
 * Handles parsing and dispatching navigation requests originating from:
 * - Firebase Cloud Messaging (FCM) notifications (Android & Web)
 * - Custom URL schemes (aispeaking://...)
 * - Web URL fragments / browser history
 */
object AppDeepLinkManager {
    private val logger = Logger.withTag("AppDeepLinkManager")
    private val _navigationRequests = MutableSharedFlow<AppRoute>(extraBufferCapacity = 64)
    val navigationRequests: SharedFlow<AppRoute> = _navigationRequests.asSharedFlow()

    /**
     * Dispatches a route navigation event to active listeners (e.g. AppNavigation).
     */
    fun navigateTo(route: AppRoute): Boolean {
        logger.i { "Dispatching navigation route: $route" }
        return _navigationRequests.tryEmit(route)
    }

    /**
     * Parses FCM data payload dictionary and triggers navigation if valid.
     * Returns true if a valid route was found and emitted.
     */
    fun handleDeepLink(data: Map<String, String>): Boolean {
        logger.d { "Handling deep link data: $data" }
        val route = parseRoute(data)
        return if (route != null) {
            navigateTo(route)
        } else {
            logger.w { "No route matched for deep link data: $data" }
            false
        }
    }

    /**
     * Parses an incoming deep link URL or URI string and triggers navigation if valid.
     * Returns true if a valid route was found and emitted.
     */
    fun handleUrl(url: String): Boolean {
        logger.d { "Handling URL: $url" }
        val route = parseUrl(url)
        return if (route != null) {
            navigateTo(route)
        } else {
            logger.w { "No route matched for URL: $url" }
            false
        }
    }

    /**
     * Maps an FCM data payload map to an [AppRoute].
     *
     * Supported parameters:
     * - "stage_id", "stageId", "stage": Navigates directly to [StageChatRoute].
     * - "screen": Target screen identifier ("chat", "stages", "subscription", "profile", "main", "login").
     * - "url", "link", "click_action": Full URI string (e.g. "aispeaking://chat?stageId=stage-01").
     */
    fun parseRoute(data: Map<String, String>): AppRoute? {
        val directUrl = data["url"] ?: data["link"] ?: data["click_action"]
        if (!directUrl.isNullOrBlank()) {
            val fromUrl = parseUrl(directUrl)
            if (fromUrl != null) return fromUrl
        }

        val stageId = data["stage_id"] ?: data["stageId"] ?: data["stage"]
        val screen = data["screen"]?.lowercase()?.trim()

        return when {
            screen == "chat" && !stageId.isNullOrBlank() -> StageChatRoute(stageId)
            !stageId.isNullOrBlank() -> StageChatRoute(stageId)
            screen == "profile" -> ProfileRoute
            screen == "subscription" || screen == "sub" || screen == "vip" -> SubscriptionRoute
            screen == "stages" || screen == "stages_list" || screen == "stageslist" -> StagesListRoute
            screen == "main" || screen == "roadmap" || screen == "map" -> MainRoute
            screen == "login" || screen == "auth" -> LoginRoute
            else -> null
        }
    }

    /**
     * Parses URI / URL string into [AppRoute].
     * Handles:
     * - aispeaking://chat?stageId=...
     * - aispeaking://stage/...
     * - aispeaking://subscription
     * - aispeaking://profile
     * - aispeaking://stages
     * - https://aispeaking.ir/#chat?stageId=...
     * - https://aispeaking.ir/#subscription
     */
    fun parseUrl(url: String): AppRoute? {
        if (url.isBlank()) return null
        val cleanUrl = url.trim()

        // Extract stage ID parameter if present in query string
        val stageIdParam = when {
            cleanUrl.contains("stageId=") -> cleanUrl.substringAfter("stageId=").substringBefore("&")
            cleanUrl.contains("stage_id=") -> cleanUrl.substringAfter("stage_id=").substringBefore("&")
            cleanUrl.contains("stage=") -> cleanUrl.substringAfter("stage=").substringBefore("&")
            else -> null
        }

        // Extract path or fragment identifier
        val fragmentOrPath = when {
            cleanUrl.contains("#") -> cleanUrl.substringAfter("#")
            cleanUrl.contains("://") -> cleanUrl.substringAfter("://")
            else -> cleanUrl
        }.lowercase()

        val normalizedRoute = fragmentOrPath.substringBefore("?").trim('/')

        return when {
            normalizedRoute.startsWith("chat") || !stageIdParam.isNullOrBlank() -> {
                val stageId = stageIdParam ?: normalizedRoute.substringAfter("chat/").takeIf { it.isNotBlank() } ?: "stage-01-inflight-london"
                StageChatRoute(stageId)
            }
            normalizedRoute.startsWith("stage/") -> {
                val stageId = fragmentOrPath.substringAfter("stage/").substringBefore("?").substringBefore("&").trim('/')
                if (stageId.isNotBlank()) StageChatRoute(stageId) else StagesListRoute
            }
            normalizedRoute == "stages" || normalizedRoute == "stages_list" || normalizedRoute == "stageslist" -> StagesListRoute
            normalizedRoute == "profile" -> ProfileRoute
            normalizedRoute == "subscription" || normalizedRoute == "sub" || normalizedRoute == "vip" -> SubscriptionRoute
            normalizedRoute == "main" || normalizedRoute == "roadmap" || normalizedRoute == "map" -> MainRoute
            normalizedRoute == "login" || normalizedRoute == "auth" -> LoginRoute
            else -> null
        }
    }
}
