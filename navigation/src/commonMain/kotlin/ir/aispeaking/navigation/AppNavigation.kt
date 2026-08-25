package ir.aispeaking.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ir.aispeaking.chat.ChatScreen
import ir.aispeaking.competition.CompetitionScreen
import ir.aispeaking.editprofile.EditProfileScreen
import ir.aispeaking.englishLevel.EnglishLevelScreen
import ir.aispeaking.feature.auth.AuthScreen
import ir.aispeaking.lightener.LightenerScreen
import ir.aispeaking.main.MainScreen
import ir.aispeaking.main.model.BottomBarItem
import ir.aispeaking.onboarding.OnboardingScreen
import ir.aispeaking.profile.ProfileScreen
import ir.aispeaking.purchases.PurchasesScreen
import ir.aispeaking.register.RegisterScreen
import ir.aispeaking.roadmap.RoadmapScreen
import ir.aispeaking.scenario_detail.ScenarioDetailScreen
import ir.aispeaking.scenarios.ScenariosScreen
import ir.aispeaking.search.SearchScreen
import ir.aispeaking.splash.SplashScreen

/**
 * Root navigation graph for the app, built with Navigation 3.
 *
 * Two back stacks are used:
 *  - [backStack]     — the outer app flow (splash -> auth -> onboarding -> main -> ...)
 *                     plus any full-screen destination pushed on top of [MainRoute]
 *                     (search, scenario detail, purchases, edit-profile, chat, ...).
 *                     These destinations render without the bottom bar.
 *  - [mainBackStack] — the inner tab flow owned by [MainScreen]. It holds only the
 *                     five bottom-nav root tabs (Scenarios / Competition / Lightener /
 *                     Roadmap / Profile). Switching tabs swaps the root of this stack.
 *
 * Design rule: every destination reached from a tab (search, detail, purchases,
 * edit-profile, chat) is pushed onto the OUTER [backStack] so it covers the whole
 * screen above [MainRoute]; pressing back returns to [MainRoute] with the active tab
 * preserved on the inner stack. Only the five tab roots themselves live on
 * [mainBackStack].
 *
 * "Logout" from any tab screen resets the whole outer stack to [AuthRoute]
 * (see [resetToAuth]) so the user cannot back into authenticated content.
 *
 * NOTE: [ChatRoute] / chat entry are kept disabled until :feature:chat compiles on
 * every CMP target (TTS/STT libs still fail on JVM/JS/WasmJs). See the chat block
 * below and the matching note in navigation/di/AppModule.kt.
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navigationConfig, SplashRoute)

    // Reset the outer stack to a single Auth entry — used by every "navigate to login"
    // (logout) callback from the bottom-nav tabs so the user cannot back into session
    // content after signing out.
    val resetToAuth: () -> Unit = {
        backStack.clear()
        backStack.add(AuthRoute)
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            // region Onboarding / auth flow
            entry<SplashRoute> {
                SplashScreen(
                    navigateToAuth = { backStack.add(AuthRoute) },
                    navigateToMain = {
                        backStack.removeAll { it is SplashRoute }
                        backStack.add(MainRoute)
                    },
                    navigateToOnboarding = {
                        backStack.removeAll { it is SplashRoute }
                        backStack.add(OnboardingRoute)
                    },
                )
            }
            entry<AuthRoute> {
                AuthScreen(
                    navigateToRegister = { mobile ->
                        backStack.add(RegisterRoute(languageLevel = "", mobile = mobile))
                    },
                    navigateToMain = {
                        backStack.removeAll { it is AuthRoute }
                        backStack.add(MainRoute)
                    },
                )
            }
            entry<OnboardingRoute> {
                OnboardingScreen(
                    navigateToMain = {
                        backStack.removeAll { it is OnboardingRoute }
                        backStack.add(MainRoute)
                    },
                )
            }
            entry<RegisterRoute> { key ->
                RegisterScreen(
                    mobile = key.mobile,
                    languageLevel = key.languageLevel,
                    navigateToLevel = { mobile ->
                        backStack.add(EnglishLevelRoute(mobile = mobile))
                    },
                    navigateToMain = {
                        // Pop the registration flow (Auth -> Register/EnglishLevel) so
                        // the back stack cannot return into it, then enter Main.
                        backStack.removeAll {
                            it is AuthRoute || it is RegisterRoute || it is EnglishLevelRoute
                        }
                        backStack.add(MainRoute)
                    },
                )
            }
            entry<EnglishLevelRoute> { key ->
                EnglishLevelScreen(
                    mobile = key.mobile,
                    navigateToBack = { backStack.removeLastOrNull() },
                    navigateToRegister = { level, mobile ->
                        // Replace any prior RegisterRoute so we don't stack duplicate
                        // register entries when bouncing back from the level picker.
                        backStack.removeAll { it is RegisterRoute }
                        backStack.add(RegisterRoute(languageLevel = level.name, mobile = mobile))
                    },
                )
            }
            // endregion

            // region Main (bottom-nav host) — tab switching only lives on [mainBackStack];
            //       pushes from tabs target the outer [backStack].
            entry<MainRoute> {
                // Inner tab stack. Seeded with the first tab (Scenarios).
                val mainBackStack = rememberNavBackStack(navigationConfig, ScenariosRoute)

                MainScreen(
                    navHost = {
                        NavDisplay(
                            backStack = mainBackStack,
                            onBack = { mainBackStack.removeLastOrNull() },
                            entryDecorators = listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberViewModelStoreNavEntryDecorator(),
                            ),
                            entryProvider = entryProvider {
                                entry<ScenariosRoute> {
                                    ScenariosScreen(
                                        navigateToSearch = { category ->
                                            backStack.add(SearchRoute(category?.id))
                                        },
                                        navigateToScenarioDetail = { scenario ->
                                            backStack.add(
                                                ScenarioDetailRoute(
                                                    id = scenario.id,
                                                    imageUrl = scenario.imageUrl,
                                                    title = scenario.title,
                                                    isChallenge = false,
                                                ),
                                            )
                                        },
                                    )
                                }
                                entry<CompetitionRoute> {
                                    CompetitionScreen(
                                        navigateToLogin = { resetToAuth() },
                                        navigateToScenarioDetail = { challenge ->
                                            backStack.add(
                                                ScenarioDetailRoute(
                                                    id = challenge.id,
                                                    imageUrl = challenge.imageUrl,
                                                    title = challenge.title,
                                                    isChallenge = true,
                                                ),
                                            )
                                        },
                                    )
                                }
                                entry<LightenerRoute> {
                                    LightenerScreen(
                                        navigateBack = { mainBackStack.removeLastOrNull() },
                                        navigateToLogin = { resetToAuth() },
                                    )
                                }
                                entry<RoadmapRoute> {
                                    RoadmapScreen(
                                        navigateBack = { mainBackStack.removeLastOrNull() },
                                        navigateToLogin = { resetToAuth() },
                                    )
                                }
                                entry<ProfileRoute> {
                                    ProfileScreen(
                                        navigateBack = { mainBackStack.removeLastOrNull() },
                                        navigateToLogin = { resetToAuth() },
                                        navigatePurchase = { backStack.add(PurchasesRoute) },
                                        navigatePaymentHistory = { backStack.add(PurchasesRoute) },
                                        navigateEditProfile = { backStack.add(EditProfileRoute) },
                                    )
                                }
                            },
                        )
                    },
                    navigateToPage = { item ->
                        val route = when (item) {
                            is BottomBarItem.Scenarios -> ScenariosRoute
                            is BottomBarItem.Challenge -> CompetitionRoute
                            is BottomBarItem.Lightener -> LightenerRoute
                            is BottomBarItem.RoadMap -> RoadmapRoute
                            is BottomBarItem.Profile -> ProfileRoute
                        }
                        // Tab switch: reset the tab stack to the chosen root so each
                        // tab starts fresh (matches the old popUpTo(0) + launchSingleTop
                        // behavior from the Nav 2 implementation).
                        if (mainBackStack.lastOrNull() != route) {
                            mainBackStack.clear()
                            mainBackStack.add(route)
                        }
                    },
                )
            }
            // endregion

            // region Full-screen destinations pushed above [MainRoute] (outer stack)
            entry<SearchRoute> { key ->
                SearchScreen(
                    categoryId = key.categoryId,
                    navigateScenario = { scenario ->
                        backStack.add(
                            ScenarioDetailRoute(
                                id = scenario.id,
                                imageUrl = scenario.imageUrl,
                                title = scenario.title,
                                isChallenge = false,
                            ),
                        )
                    },
                    navigateBack = { backStack.removeLastOrNull() },
                )
            }
            entry<ScenarioDetailRoute> { key ->
                ScenarioDetailScreen(
                    scenarioId = key.id,
                    scenarioImageUrl = key.imageUrl ?: "",
                    scenarioTitle = key.title,
                    isChallenge = key.isChallenge,
                    navigateBack = { backStack.removeLastOrNull() },
                    navigateToLogin = { resetToAuth() },
                    navigateToPurchase = { backStack.add(PurchasesRoute) },
                    navigateToChat = { level ->
                        backStack.add(ChatRoute(level = level::class.simpleName!!))
                    },
                )
            }
            entry<EditProfileRoute> {
                EditProfileScreen(
                    navigateToLogin = { resetToAuth() },
                    navigateBack = { backStack.removeLastOrNull() },
                )
            }
            entry<PurchasesRoute> {
                PurchasesScreen(
                    navigateBack = { backStack.removeLastOrNull() },
                )
            }

            entry<ChatRoute> { key ->
                ChatScreen(
                    level = key.level,
                    navigateBack = { backStack.removeLastOrNull() },
                )
            }
            // endregion
        },
    )
}
