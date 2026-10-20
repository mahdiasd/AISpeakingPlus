package ir.aispeaking.web

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.terrakok.navigation3.browser.ChronologicalBrowserNavigation
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName
import ir.aispeaking.navigation.LoginRoute
import ir.aispeaking.navigation.MainRoute
import ir.aispeaking.navigation.ProfileRoute
import ir.aispeaking.navigation.SplashRoute
import ir.aispeaking.navigation.StageChatRoute
import ir.aispeaking.navigation.StagesListRoute
import ir.aispeaking.navigation.SubscriptionRoute

@Composable
fun WebBrowserNavigation(backStack: NavBackStack<NavKey>) {
    ChronologicalBrowserNavigation(
        backStack = backStack,
        saveKey = { key ->
            when (key) {
                is SplashRoute -> buildBrowserHistoryFragment("splash")
                is LoginRoute -> buildBrowserHistoryFragment("login")
                is MainRoute -> buildBrowserHistoryFragment("main")
                is StageChatRoute -> buildBrowserHistoryFragment("chat", mapOf("stageId" to key.stageId))
                is ProfileRoute -> buildBrowserHistoryFragment("profile")
                is SubscriptionRoute -> buildBrowserHistoryFragment("subscription")
                is StagesListRoute -> buildBrowserHistoryFragment("stages")
                else -> null
            }
        },
        restoreKey = { rawFragment ->
            val fragment = if (rawFragment.contains("%3F", ignoreCase = true) || rawFragment.contains("%3D", ignoreCase = true)) {
                rawFragment.replace("%3F", "?", ignoreCase = true)
                    .replace("%3D", "=", ignoreCase = true)
                    .replace("%26", "&", ignoreCase = true)
            } else {
                rawFragment
            }
            val fragmentName = getBrowserHistoryFragmentName(fragment)
            when {
                fragmentName == "splash" -> SplashRoute
                fragmentName == "login" -> LoginRoute
                fragmentName == "main" || fragmentName == "roadmap" -> MainRoute
                fragmentName == "profile" -> ProfileRoute
                fragmentName == "subscription" || fragmentName == "sub" -> SubscriptionRoute
                fragmentName == "stages" || fragmentName == "stages_list" -> StagesListRoute
                fragmentName == "chat" || fragmentName?.startsWith("chat") == true -> {
                    val params = parseFragmentParameters(fragment)
                    val stageId = params["stageId"]
                        ?: params["stage_id"]
                        ?: when {
                            fragment.contains("stageId=") -> fragment.substringAfter("stageId=").substringBefore("&")
                            fragment.contains("stage_id=") -> fragment.substringAfter("stage_id=").substringBefore("&")
                            else -> null
                        }
                    StageChatRoute(stageId?.takeIf { it.isNotBlank() } ?: "stage-01-inflight-london")
                }
                else -> null
            }
        }
    )
}

private fun parseFragmentParameters(fragment: String): Map<String, String> {
    val query = fragment.substringAfter('?', "")
    if (query.isEmpty()) return emptyMap()
    return query.split('&')
        .mapNotNull { param ->
            val parts = param.split('=', limit = 2)
            if (parts.size == 2) parts[0] to parts[1] else null
        }
        .toMap()
}
