package ir.aispeaking.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Central, type-safe Navigation 3 route definitions for the whole app.
 *
 * Every route is a `@Serializable` [NavKey] so the back stack can be saved and
 * restored across process death and configuration changes (see [navigationConfig]).
 *
 * Routes are grouped under the sealed [AppRoute] hierarchy purely for
 * exhaustiveness and discoverability; Nav 3 itself only requires [NavKey].
 *
 * NOTE: This file is the single source of truth for route keys. Feature modules
 * no longer declare their own route classes (the old per-feature
 * `navigation/<feature>Screen.kt` duplicates were removed during the Nav 2 -> Nav 3
 * cleanup).
 */
@Serializable
sealed interface AppRoute : NavKey {
    val screenName: String
}

// region Onboarding / auth flow
@Serializable
data object SplashRoute : AppRoute {
    override val screenName: String = "Splash"
}

@Serializable
data object OnboardingRoute : AppRoute {
    override val screenName: String = "Onboarding"
}

@Serializable
data object AuthRoute : AppRoute {
    override val screenName: String = "Auth"
}

@Serializable
data class RegisterRoute(
    val languageLevel: String,
    val mobile: String,
) : AppRoute {
    override val screenName: String = "Register"
}
// endregion

// region Main destinations
@Serializable
data object CharacterHomeRoute : AppRoute {
    override val screenName: String = "CharacterHome"
}

@Serializable
data object MainRoute : AppRoute {
    override val screenName: String = "Main"
}

@Serializable
data object ProfileRoute : AppRoute {
    override val screenName: String = "Profile"
}
// endregion

// region Detail / pushed screens
@Serializable
data class ChatRoute(
    val level: String,
) : AppRoute {
    override val screenName: String = "Chat"
}

@Serializable
data object EditProfileRoute : AppRoute {
    override val screenName: String = "EditProfile"
}

@Serializable
data object PurchasesRoute : AppRoute {
    override val screenName: String = "Purchases"
}
// endregion
