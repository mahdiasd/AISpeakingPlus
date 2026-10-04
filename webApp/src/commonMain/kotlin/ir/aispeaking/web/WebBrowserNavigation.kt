package ir.aispeaking.web

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.github.terrakok.navigation3.browser.ChronologicalBrowserNavigation
import com.github.terrakok.navigation3.browser.buildBrowserHistoryFragment
import com.github.terrakok.navigation3.browser.getBrowserHistoryFragmentName
import ir.aispeaking.navigation.LoginRoute
import ir.aispeaking.navigation.MainRoute
import ir.aispeaking.navigation.SplashRoute

@Composable
fun WebBrowserNavigation(backStack: NavBackStack<NavKey>) {
    ChronologicalBrowserNavigation(
        backStack = backStack,
        saveKey = { key ->
            when (key) {
                is SplashRoute -> buildBrowserHistoryFragment("splash")
                is LoginRoute -> buildBrowserHistoryFragment("login")
                is MainRoute -> buildBrowserHistoryFragment("main")
                else -> null
            }
        },
        restoreKey = { fragment ->
            when (getBrowserHistoryFragmentName(fragment)) {
                "splash" -> SplashRoute
                "login" -> LoginRoute
                "main", "roadmap" -> MainRoute
                else -> null
            }
        }
    )
}
