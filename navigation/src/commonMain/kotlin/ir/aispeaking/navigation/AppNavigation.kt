package ir.aispeaking.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ir.aispeaking.auth.LoginRootScreen
import ir.aispeaking.chat.ChatScreen
import ir.aispeaking.main.JourneyMapScreen
import ir.aispeaking.profile.ProfileScreen
import ir.aispeaking.splash.SplashRoute
import ir.aispeaking.stageslist.StagesListScreen
import ir.aispeaking.subscription.SubscriptionScreen
import androidx.compose.runtime.LaunchedEffect
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.analytics.logScreenView
import org.koin.compose.koinInject
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

    val analyticsTracker = koinInject<AnalyticsTracker>()
    val currentRoute = backStack.lastOrNull()

    LaunchedEffect(currentRoute) {
        if (currentRoute is AppRoute) {
            analyticsTracker.logScreenView(
                screenName = currentRoute.screenName,
                screenClass = currentRoute::class.simpleName
            )
        }
    }

    LaunchedEffect(Unit) {
        AppDeepLinkManager.navigationRequests.collect { route ->
            if (route is StageChatRoute || route is ProfileRoute || route is SubscriptionRoute || route is StagesListRoute) {
                if (backStack.none { it is MainRoute }) {
                    backStack.removeAll { it is SplashRoute }
                    backStack.add(MainRoute)
                }
            }
            if (backStack.lastOrNull() != route) {
                backStack.add(route)
            }
        }
    }

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
                LoginRootScreen(
                    onNavigateToMain = {
                        backStack.clear()
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
                    },
                    onNavigateToSubscription = {
                        backStack.add(SubscriptionRoute)
                    },
                    onNavigateToStagesList = {
                        backStack.add(StagesListRoute)
                    }
                )
            }

            entry<StagesListRoute> {
                StagesListScreen(
                    viewModel = koinViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToStageChat = { stageId ->
                        backStack.add(StageChatRoute(stageId))
                    },
                    onNavigateToSubscription = {
                        backStack.add(SubscriptionRoute)
                    },
                    onNavigateToLogin = {
                        backStack.add(LoginRoute)
                    }
                )
            }

            entry<ProfileRoute> {
                ProfileScreen(
                    viewModel = koinViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToLogin = {
                        backStack.clear()
                        backStack.add(LoginRoute)
                    },
                    onNavigateToSubscription = {
                        backStack.add(SubscriptionRoute)
                    }
                )
            }

            entry<StageChatRoute> { route ->
                ChatScreen(
                    stageId = route.stageId,
                    viewModel = koinViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() }
                )
            }

            entry<SubscriptionRoute> {
                SubscriptionScreen(
                    viewModel = koinViewModel(),
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onSubscriptionSuccess = {
                        backStack.removeLastOrNull()
                    }
                )
            }
        }
    )
}

