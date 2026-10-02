package ir.aispeaking.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ir.aispeaking.sharedui.ui.login.LoginScreen
import ir.aispeaking.sharedui.ui.splash.SplashScreen
import ir.aispeaking.sharedui.ui.stage.JourneyMapScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root navigation graph for AISpeakingPlus built with Navigation 3.
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navigationConfig, SplashRoute)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SplashRoute> {
                SplashScreen(
                    onNavigateToMain = {
                        backStack.removeAll { it is SplashRoute }
                        backStack.add(MainRoute)
                    },
                    onNavigateToLogin = {
                        backStack.removeAll { it is SplashRoute }
                        backStack.add(LoginRoute)
                    }
                )
            }

            entry<LoginRoute> {
                LoginScreen(
                    onNavigateToMain = {
                        backStack.removeAll { it is LoginRoute }
                        backStack.add(MainRoute)
                    }
                )
            }

            entry<MainRoute> {
                JourneyMapScreen(
                    viewModel = koinViewModel(),
                    onNavigateToChat = { stageId ->
                        // Stage Chat entry point
                    }
                )
            }
        }
    )
}
