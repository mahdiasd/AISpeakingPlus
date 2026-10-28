@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package ir.aispeaking.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import ir.aispeaking.navigation.AppRoute
import ir.aispeaking.navigation.IntroVideoRoute
import ir.aispeaking.navigation.LoginRoute
import ir.aispeaking.navigation.MainRoute
import ir.aispeaking.navigation.ProfileRoute
import ir.aispeaking.navigation.SplashRoute
import ir.aispeaking.navigation.StageChatRoute
import ir.aispeaking.navigation.StagesListRoute
import ir.aispeaking.navigation.SubscriptionRoute
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

fun exitWebApp() {
    try {
        jsExitApp()
    } catch (_: Throwable) {}
}

/**
 * Modern HTML5 History navigation bridge for Compose Multiplatform.
 * Eliminates URL hash fragments (#main) and maps Compose NavBackStack to clean paths
 * (e.g. /app/main, /app/chat?stageId=..., /app/stages, /app/profile, /app/intro).
 * Automatically cleans up any legacy hash bookmarks and synchronizes with browser Back/Forward.
 */
@Composable
fun WebBrowserNavigation(backStack: NavBackStack<NavKey>) {
    LaunchedEffect(backStack) {
        // 1. Initial URL inspection (detect deep-links or legacy hash fragments)
        val initialHash = jsGetHash()
        val initialPath = jsGetCurrentPath()
        val initialSearch = jsGetCurrentSearch()

        var initialRoute: AppRoute? = null
        if (initialHash.isNotBlank()) {
            initialRoute = parseLegacyHash(initialHash)
        }
        if (initialRoute == null) {
            initialRoute = urlToRoute(initialPath, initialSearch)
        }

        if (initialRoute != null && initialRoute !is SplashRoute) {
            // Restore deep link or converted legacy hash cleanly
            if (initialRoute is MainRoute || initialRoute is LoginRoute || initialRoute is IntroVideoRoute) {
                backStack.clear()
                backStack.add(initialRoute)
            } else {
                backStack.clear()
                backStack.add(MainRoute)
                backStack.add(initialRoute)
            }
            jsReplaceUrl(routeToUrl(initialRoute))
        }

        // 2. Observe backStack changes and push clean HTML5 paths to window.history
        var previousWasSplash = (backStack.lastOrNull() is SplashRoute)

        launch {
            snapshotFlow { backStack.toList() }.collect { stack ->
                val top = stack.lastOrNull() ?: return@collect
                val targetUrl = routeToUrl(top)
                val currentUrl = jsGetCurrentPath() + jsGetCurrentSearch()

                if (targetUrl != currentUrl) {
                    if (top is SplashRoute || previousWasSplash) {
                        jsReplaceUrl(targetUrl)
                    } else {
                        jsPushUrl(targetUrl)
                    }
                }

                previousWasSplash = (top is SplashRoute)
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
        is ProfileRoute -> "/app/profile"
        is SubscriptionRoute -> "/app/subscription"
        is LoginRoute -> "/app/login"
        else -> "/app/main"
    }
}

private fun urlToRoute(path: String, search: String): AppRoute? {
    val cleanPath = path.substringAfter("/app").trim('/')
    val params = parseQueryParameters(search)

    return when {
        cleanPath == "intro" || cleanPath == "video" || cleanPath == "prologue" -> IntroVideoRoute
        cleanPath == "main" || cleanPath == "roadmap" -> MainRoute
        cleanPath == "profile" -> ProfileRoute
        cleanPath == "subscription" || cleanPath == "sub" -> SubscriptionRoute
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
        name == "subscription" || name == "sub" -> SubscriptionRoute
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
