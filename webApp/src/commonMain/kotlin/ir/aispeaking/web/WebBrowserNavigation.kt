@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import ir.aispeaking.navigation.AppRoute
import ir.aispeaking.navigation.IntroVideoRoute
import ir.aispeaking.navigation.LeaderboardRoute
import ir.aispeaking.navigation.LeitnerRoute
import ir.aispeaking.navigation.LoginRoute
import ir.aispeaking.navigation.MainRoute
import ir.aispeaking.navigation.PaymentResultRoute
import ir.aispeaking.navigation.PlacementTestRoute
import ir.aispeaking.navigation.ProfileRoute
import ir.aispeaking.navigation.SplashRoute
import ir.aispeaking.navigation.StageChatRoute
import ir.aispeaking.navigation.StagesListRoute
import ir.aispeaking.navigation.SubscriptionRoute
import ir.aispeaking.sharedui.ui.navigation.BackNavigationManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@JsFun("() => window.aiSpeakingRouter ? window.aiSpeakingRouter.getCurrentPath() : window.location.pathname")
private external fun jsGetCurrentPath(): String

@JsFun("() => window.aiSpeakingRouter ? window.aiSpeakingRouter.getCurrentSearch() : (window.location.search || '')")
private external fun jsGetCurrentSearch(): String

@JsFun("() => window.aiSpeakingRouter ? window.aiSpeakingRouter.getHash() : (window.location.hash || '')")
private external fun jsGetHash(): String

@JsFun("(url) => window.aiSpeakingRouter ? window.aiSpeakingRouter.pushUrl(url) : window.history.pushState(null, '', url)")
private external fun jsPushUrl(url: String)

@JsFun("(url) => window.aiSpeakingRouter ? window.aiSpeakingRouter.replaceUrl(url) : window.history.replaceState(null, '', url)")
private external fun jsReplaceUrl(url: String)

@JsFun("() => window.aiSpeakingRouter ? window.aiSpeakingRouter.getPopCounter() : 0")
private external fun jsGetPopCounter(): Int

@JsFun("() => { try { window.close(); } catch(e){} window.location.href = '/'; }")
private external fun jsExitApp()

@JsFun("() => { if (window.hideAppLoader) window.hideAppLoader(); }")
private external fun jsHideAppLoader()

fun exitWebApp() {
    try {
        jsExitApp()
    } catch (_: Throwable) {}
}

fun hideWebLoader() {
    try {
        jsHideAppLoader()
    } catch (_: Throwable) {}
}

fun resolveInitialWebRoute(hasSeenIntroVideo: Boolean = true): AppRoute {
    return resolveInitialWebRoute(
        path = jsGetCurrentPath(),
        search = jsGetCurrentSearch(),
        hash = jsGetHash(),
        hasSeenIntroVideo = hasSeenIntroVideo
    )
}

internal fun resolveInitialWebRoute(
    path: String,
    search: String,
    hash: String,
    hasSeenIntroVideo: Boolean
): AppRoute {
    val defaultEntryRoute: AppRoute = if (hasSeenIntroVideo) MainRoute else IntroVideoRoute
    if (hash.isNotBlank()) {
        val parsed = parseLegacyHash(hash)
        if (parsed != null && parsed !is SplashRoute) {
            return if (parsed is MainRoute && !hasSeenIntroVideo) IntroVideoRoute else parsed
        }
    }
    val cleanPath = path.substringAfter("/app").trim('/')
    if (cleanPath.isEmpty()) {
        return defaultEntryRoute
    }
    val route = urlToRoute(path, search)
    return when {
        route == null || route is SplashRoute -> defaultEntryRoute
        route is MainRoute && !hasSeenIntroVideo -> IntroVideoRoute
        else -> route
    }
}

/**
 * Modern HTML5 History navigation bridge for Compose Multiplatform.
 * Eliminates URL hash fragments (#main) and maps Compose NavBackStack to clean paths
 * (e.g. /app/main, /app/chat?stageId=..., /app/stages, /app/profile, /app/intro).
 * Automatically cleans up any legacy hash bookmarks and synchronizes with browser Back/Forward.
 */
@Composable
fun WebBrowserNavigation(
    backStack: NavBackStack<NavKey>,
    onRouteChanged: (AppRoute?) -> Unit = {}
) {
    LaunchedEffect(backStack) {
        // 1. Synchronize initial backStack (resolved by resolveInitialWebRoute) with browser URL
        val initialTop = backStack.lastOrNull() as? AppRoute
        if (initialTop != null && initialTop !is SplashRoute) {
            if (initialTop !is MainRoute && initialTop !is LoginRoute && initialTop !is IntroVideoRoute && backStack.none { it is MainRoute }) {
                backStack.clear()
                backStack.add(MainRoute)
                backStack.add(initialTop)
            }
            jsReplaceUrl(routeToUrl(initialTop))
        }
        onRouteChanged(backStack.lastOrNull() as? AppRoute)

        // 2. Observe backStack changes and push clean HTML5 paths to window.history
        var previousWasTransientRoot = (
            backStack.lastOrNull() is SplashRoute ||
                (backStack.size == 1 && backStack.lastOrNull() is IntroVideoRoute)
            )

        launch {
            snapshotFlow { backStack.toList() }.collect { stack ->
                val top = stack.lastOrNull() ?: return@collect
                onRouteChanged(top as? AppRoute)
                val targetUrl = routeToUrl(top)
                val currentUrl = jsGetCurrentPath() + jsGetCurrentSearch()

                if (targetUrl != currentUrl) {
                    if (top is SplashRoute || previousWasTransientRoot) {
                        jsReplaceUrl(targetUrl)
                    } else {
                        jsPushUrl(targetUrl)
                    }
                }

                previousWasTransientRoot = (
                    top is SplashRoute ||
                        (stack.size == 1 && top is IntroVideoRoute)
                    )
            }
        }

        // 3. Listen to browser Back / Forward (popstate)
        launch {
            var lastPopCounter = jsGetPopCounter()
            while (isActive) {
                delay(60)
                val currentPop = jsGetPopCounter()
                if (currentPop != lastPopCounter) {
                    lastPopCounter = currentPop

                    // If an active screen (e.g. ChatScreen) intercepts the back action:
                    val currentTop = backStack.lastOrNull()
                    if (BackNavigationManager.handleBack()) {
                        // Restore current active URL into browser history so URL doesn't revert
                        if (currentTop != null) {
                            val currentUrl = routeToUrl(currentTop)
                            jsPushUrl(currentUrl)
                        }
                        continue
                    }

                    val path = jsGetCurrentPath()
                    val search = jsGetCurrentSearch()
                    val route = urlToRoute(path, search) ?: MainRoute

                    if (backStack.lastOrNull() != route) {
                        if (route is MainRoute) {
                            backStack.clear()
                            backStack.add(MainRoute)
                        } else if (route is LoginRoute || route is IntroVideoRoute) {
                            backStack.clear()
                            backStack.add(route)
                        } else {
                            if (backStack.none { it is MainRoute }) {
                                backStack.clear()
                                backStack.add(MainRoute)
                            }
                            if (backStack.lastOrNull() != route) {
                                backStack.add(route)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun routeToUrl(route: NavKey): String {
    return when (route) {
        is SplashRoute -> "/app"
        is IntroVideoRoute -> "/app/intro"
        is MainRoute -> "/app/main"
        is StageChatRoute -> "/app/chat?stageId=${route.stageId}"
        is StagesListRoute -> "/app/stages"
        is LeaderboardRoute -> "/app/leaderboard"
        is ProfileRoute -> "/app/profile"
        is SubscriptionRoute -> "/app/subscription"
        is LeitnerRoute -> "/app/leitner"
        is PlacementTestRoute -> "/app/placement"
        is PaymentResultRoute -> {
            val status = if (route.isSuccess) "success" else "failed"
            val refParam = route.refId?.let { "&ref_id=$it" }.orEmpty()
            val planParam = route.planTitle?.let { "&plan=$it" }.orEmpty()
            val errParam = route.errorMessage?.let { "&error=$it" }.orEmpty()
            val marketParam = route.market?.let { "&market=$it" }.orEmpty()
            "/app/payment-result?status=$status$refParam$planParam$errParam$marketParam"
        }
        is LoginRoute -> "/app/login"
        else -> "/app/main"
    }
}

private fun urlToRoute(path: String, search: String): AppRoute? {
    val cleanPath = path.substringAfter("/app").trim('/')
    val params = parseQueryParameters(search)

    return when {
        cleanPath.isEmpty() || cleanPath == "main" || cleanPath == "roadmap" -> MainRoute
        cleanPath == "intro" || cleanPath == "video" || cleanPath == "prologue" -> IntroVideoRoute
        cleanPath == "profile" -> ProfileRoute
        cleanPath == "leaderboard" || cleanPath == "ranking" || cleanPath == "podium" -> LeaderboardRoute
        cleanPath == "subscription" || cleanPath == "sub" -> SubscriptionRoute
        cleanPath == "leitner" || cleanPath == "box" -> LeitnerRoute
        cleanPath == "placement" || cleanPath == "placement-test" || cleanPath == "placement_test" -> PlacementTestRoute
        cleanPath == "payment-result" || cleanPath == "payment/result" -> {
            val status = params["status"] ?: params["payment_status"] ?: "failed"
            val isSuccess = status.equals("success", ignoreCase = true) || status.equals("ok", ignoreCase = true)
            val refId = params["ref_id"] ?: params["refId"] ?: params["authority"]
            val planTitle = params["plan"] ?: params["plan_title"]
            val error = params["error"] ?: params["message"]
            PaymentResultRoute(
                isSuccess = isSuccess,
                refId = refId,
                planTitle = planTitle,
                orderId = params["order_id"] ?: params["orderId"],
                errorMessage = error,
                market = params["market"]
            )
        }
        cleanPath == "subscription" && (params.containsKey("payment_status") || params.containsKey("status")) -> {
            val status = params["payment_status"] ?: params["status"] ?: "failed"
            val isSuccess = status.equals("success", ignoreCase = true) || status.equals("ok", ignoreCase = true)
            val refId = params["ref_id"] ?: params["authority"]
            PaymentResultRoute(
                isSuccess = isSuccess,
                refId = refId,
                planTitle = params["plan"],
                errorMessage = params["error"],
                market = "DIRECT_ZARINPAL"
            )
        }
        cleanPath == "stages" || cleanPath == "stages_list" -> StagesListRoute
        cleanPath == "login" || cleanPath == "auth" -> LoginRoute
        cleanPath == "chat" || cleanPath.startsWith("chat/") -> {
            val stageId = params["stageId"]
                ?: params["stage_id"]
                ?: cleanPath.substringAfter("chat/").takeIf { it.isNotBlank() }
                ?: "stage-01-inflight-london"
            StageChatRoute(stageId)
        }
        else -> null
    }
}

private fun parseLegacyHash(hash: String): AppRoute? {
    if (hash.isBlank()) return null
    val clean = hash.trimStart('#')
    val name = clean.substringBefore('?')
    val query = clean.substringAfter('?', "")
    val params = parseQueryParameters("?$query")

    return when {
        name == "intro" || name == "video" || name == "prologue" -> IntroVideoRoute
        name == "main" || name == "roadmap" -> MainRoute
        name == "profile" -> ProfileRoute
        name == "leaderboard" || name == "ranking" -> LeaderboardRoute
        name == "subscription" && params.containsKey("payment_status") -> {
            val status = params["payment_status"] ?: "failed"
            val isSuccess = status.equals("success", ignoreCase = true) || status.equals("ok", ignoreCase = true)
            PaymentResultRoute(
                isSuccess = isSuccess,
                refId = params["ref_id"] ?: params["authority"],
                errorMessage = params["error"],
                market = "DIRECT_ZARINPAL"
            )
        }
        name == "payment-result" || name == "payment_result" -> {
            val status = params["status"] ?: params["payment_status"] ?: "failed"
            val isSuccess = status.equals("success", ignoreCase = true) || status.equals("ok", ignoreCase = true)
            val refId = params["ref_id"] ?: params["refId"] ?: params["authority"]
            val planTitle = params["plan"] ?: params["plan_title"]
            val error = params["error"] ?: params["message"]
            PaymentResultRoute(
                isSuccess = isSuccess,
                refId = refId,
                planTitle = planTitle,
                orderId = params["order_id"] ?: params["orderId"],
                errorMessage = error,
                market = params["market"]
            )
        }
        name == "placement" || name == "placement-test" || name == "placement_test" -> PlacementTestRoute
        name == "subscription" || name == "sub" -> SubscriptionRoute
        name == "leitner" || name == "box" -> LeitnerRoute
        name == "stages" || name == "stages_list" -> StagesListRoute
        name == "login" || name == "auth" -> LoginRoute
        name == "chat" || name.startsWith("chat") -> {
            val stageId = params["stageId"] ?: params["stage_id"] ?: "stage-01-inflight-london"
            StageChatRoute(stageId)
        }
        else -> null
    }
}

private fun parseQueryParameters(query: String): Map<String, String> {
    val cleanQuery = query.substringAfter('?', "")
    if (cleanQuery.isEmpty()) return emptyMap()
    return cleanQuery.split('&')
        .mapNotNull { param ->
            val parts = param.split('=', limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        .toMap()
}
