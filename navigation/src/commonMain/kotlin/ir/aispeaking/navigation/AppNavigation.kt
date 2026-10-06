package ir.aispeaking.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ir.aispeaking.sharedui.ui.login.LoginRoute
import ir.aispeaking.sharedui.ui.splash.SplashRoute
import ir.aispeaking.sharedui.ui.stage.JourneyMapScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root navigation graph for AISpeakingPlus built with Navigation 3.
 */
@Composable
fun AppNavigation(
    onBackStackCreated: @Composable (NavBackStack<NavKey>) -> Unit = {}
) {
    val backStack = rememberNavBackStack(navigationConfig, SplashRoute)
    onBackStackCreated(backStack)

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SplashRoute> {
                SplashRoute(
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
                LoginRoute(
                    onNavigateToMain = {
                        backStack.add(MainRoute)
                    }
                )
            }

            entry<MainRoute> {
                JourneyMapScreen(
                    viewModel = koinViewModel(),
                    onNavigateToChat = { stageId ->
                        backStack.add(StageChatRoute(stageId))
                    },
                    onNavigateToProfile = {
                        backStack.add(ProfileRoute)
                    }
                )
            }

            entry<ProfileRoute> {
                ir.aispeaking.sharedui.ui.profile.ProfileScreen(
                    viewModel = koinViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToLogin = { backStack.add(LoginRoute) },
                    onNavigateToSubscription = {}
                )
            }

            entry<StageChatRoute> { route ->
                ir.aispeaking.sharedui.ui.stage.ChatScreen(
                    stageId = route.stageId,
                    viewModel = koinViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
