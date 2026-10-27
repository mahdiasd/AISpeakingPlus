package ir.aispeaking.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ir.aispeaking.auth.LoginRootScreen
import ir.aispeaking.chat.ChatScreen
import ir.aispeaking.domain.model.Announcement
import ir.aispeaking.domain.usecase.announcement.GetActiveAnnouncementUseCase
import ir.aispeaking.domain.usecase.announcement.SuppressAnnouncementUseCase
import ir.aispeaking.domain.usecase.auth.SetHasSeenIntroVideoUseCase
import ir.aispeaking.main.IntroVideoScreen
import ir.aispeaking.main.JourneyMapScreen
import ir.aispeaking.profile.ProfileScreen
import ir.aispeaking.sharedui.ui.dialog.AnnouncementDialog
import ir.aispeaking.splash.SplashRoute
import ir.aispeaking.stageslist.StagesListScreen
import ir.aispeaking.subscription.SubscriptionScreen
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.analytics.logScreenView
import kotlinx.coroutines.launch
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
    val getActiveAnnouncementUseCase = koinInject<GetActiveAnnouncementUseCase>()
    val suppressAnnouncementUseCase = koinInject<SuppressAnnouncementUseCase>()
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var activeAnnouncement by remember { mutableStateOf<Announcement?>(null) }
    var showAnnouncementDialog by remember { mutableStateOf(false) }

    val currentRoute = backStack.lastOrNull()

    // Fetch active announcement on startup
    LaunchedEffect(Unit) {
        try {
            val announcement = getActiveAnnouncementUseCase()
            if (announcement != null) {
                activeAnnouncement = announcement
                showAnnouncementDialog = true
            }
        } catch (_: Throwable) {}
    }

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

    val handleAction: (action: String?, value: String?) -> Unit = { action, value ->
        val current = activeAnnouncement
        val isBlocking = current?.isBlocking == true

        when (action?.uppercase()?.trim()) {
            "OPEN_URL" -> {
                if (!value.isNullOrBlank()) {
                    try {
                        uriHandler.openUri(value)
                    } catch (_: Throwable) {}
                }
                if (!isBlocking) {
                    showAnnouncementDialog = false
                }
            }
            "NAVIGATE" -> {
                if (!isBlocking) {
                    showAnnouncementDialog = false
                }
                when (value?.lowercase()?.trim()) {
                    "subscription" -> {
                        if (backStack.lastOrNull() !is SubscriptionRoute) {
                            backStack.add(SubscriptionRoute)
                        }
                    }
                    "profile" -> {
                        if (backStack.lastOrNull() !is ProfileRoute) {
                            backStack.add(ProfileRoute)
                        }
                    }
                    "stages" -> {
                        if (backStack.lastOrNull() !is StagesListRoute) {
                            backStack.add(StagesListRoute)
                        }
                    }
                }
            }
            "RELOAD" -> {
                coroutineScope.launch {
                    val fresh = getActiveAnnouncementUseCase()
                    if (fresh == null) {
                        showAnnouncementDialog = false
                        activeAnnouncement = null
                    } else {
                        activeAnnouncement = fresh
                        showAnnouncementDialog = true
                    }
                }
            }
            "CLOSE" -> {
                if (!isBlocking) {
                    showAnnouncementDialog = false
                }
            }
            else -> {
                if (!isBlocking) {
                    showAnnouncementDialog = false
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        onNavigateToIntroVideo = {
                            backStack.removeAll { it is SplashRoute }
                            backStack.add(IntroVideoRoute)
                        },
                        onNavigateToLogin = {
                            backStack.removeAll { it is SplashRoute }
                            backStack.add(LoginRoute)
                        }
                    )
                }

                entry<IntroVideoRoute> {
                    val setHasSeenIntroVideoUseCase = koinInject<SetHasSeenIntroVideoUseCase>()
                    IntroVideoScreen(
                        onFinished = {
                            setHasSeenIntroVideoUseCase(true)
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            } else {
                                backStack.clear()
                                backStack.add(MainRoute)
                            }
                        },
                        onSkip = {
                            setHasSeenIntroVideoUseCase(true)
                            if (backStack.size > 1) {
                                backStack.removeLastOrNull()
                            } else {
                                backStack.clear()
                                backStack.add(MainRoute)
                            }
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
                        },
                        onNavigateToIntroVideo = {
                            backStack.add(IntroVideoRoute)
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

        // Global In-App Announcement Overlay Dialog
        val currentAnnouncement = activeAnnouncement
        if (showAnnouncementDialog && currentAnnouncement != null) {
            AnnouncementDialog(
                visible = true,
                announcement = currentAnnouncement,
                onDismiss = {
                    if (!currentAnnouncement.isBlocking) {
                        showAnnouncementDialog = false
                    }
                },
                onPrimaryClick = {
                    handleAction(currentAnnouncement.primaryButtonAction, currentAnnouncement.primaryButtonValue)
                },
                onSecondaryClick = {
                    handleAction(currentAnnouncement.secondaryButtonAction, currentAnnouncement.secondaryButtonValue)
                },
                onSuppressionToggle = { suppressed ->
                    if (suppressed) {
                        suppressAnnouncementUseCase(currentAnnouncement.id, 24)
                    }
                }
            )
        }
    }
}
