package ir.aispeaking.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import ir.aispeaking.chat.ChatScreen
import ir.aispeaking.editprofile.EditProfileScreen
import ir.aispeaking.feature.auth.AuthScreen
import ir.aispeaking.main.screen.CharacterHomeScreen
import ir.aispeaking.onboarding.OnboardingScreen
import ir.aispeaking.profile.ProfileScreen
import ir.aispeaking.purchases.PurchasesScreen
import ir.aispeaking.register.RegisterScreen
import ir.aispeaking.splash.SplashScreen

/**
 * Root navigation graph for the app, built with Navigation 3.
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(navigationConfig, CharacterHomeRoute)

    // Reset the outer stack to a single Auth entry — used by logout callbacks
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
                        backStack.add(CharacterHomeRoute)
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
                        backStack.add(CharacterHomeRoute)
                    },
                )
            }
            entry<OnboardingRoute> {
                OnboardingScreen(
                    navigateToMain = {
                        backStack.removeAll { it is OnboardingRoute }
                        backStack.add(CharacterHomeRoute)
                    },
                )
            }
            entry<RegisterRoute> { key ->
                RegisterScreen(
                    mobile = key.mobile,
                    languageLevel = key.languageLevel,
                    navigateToLevel = { _ ->
                        // English level picker removed in clean slate; handled in new story flow
                    },
                    navigateToMain = {
                        backStack.removeAll {
                            it is AuthRoute || it is RegisterRoute
                        }
                        backStack.add(CharacterHomeRoute)
                    },
                )
            }
            // endregion

            // region Main destinations
            entry<CharacterHomeRoute> {
                CharacterHomeScreen(
                    onNavigateToMap = {
                        // Adventure map placeholder until 001-story-based-speaking-journey is implemented
                    },
                    onNavigateToProfile = {
                        backStack.add(ProfileRoute)
                    },
                    onNavigateToLeaderboard = {
                        // Leaderboard placeholder until 001-story-based-speaking-journey is implemented
                    },
                    onNavigateToLightener = {
                        // Review/practice placeholder
                    },
                    onNavigateToStore = {
                        backStack.add(PurchasesRoute)
                    },
                )
            }

            entry<MainRoute> {
                CharacterHomeScreen(
                    onNavigateToMap = { },
                    onNavigateToProfile = {
                        backStack.add(ProfileRoute)
                    },
                    onNavigateToLeaderboard = { },
                    onNavigateToLightener = { },
                    onNavigateToStore = {
                        backStack.add(PurchasesRoute)
                    },
                )
            }

            entry<ProfileRoute> {
                ProfileScreen(
                    navigateBack = { backStack.removeLastOrNull() },
                    navigateToLogin = { resetToAuth() },
                    navigatePurchase = { backStack.add(PurchasesRoute) },
                    navigatePaymentHistory = { backStack.add(PurchasesRoute) },
                    navigateEditProfile = { backStack.add(EditProfileRoute) },
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

