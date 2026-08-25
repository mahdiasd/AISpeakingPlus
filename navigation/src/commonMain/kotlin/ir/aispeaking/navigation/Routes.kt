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

@Serializable
data class EnglishLevelRoute(
    val mobile: String,
) : AppRoute {
    override val screenName: String = "EnglishLevel"
}
// endregion

// region Main (bottom-nav host)
@Serializable
data object MainRoute : AppRoute {
    override val screenName: String = "Main"
}
// endregion

// region Bottom-nav tabs (rendered inside [MainRoute]'s nested NavDisplay)
@Serializable
data object ScenariosRoute : AppRoute {
    override val screenName: String = "Scenarios"
}

@Serializable
data object CompetitionRoute : AppRoute {
    override val screenName: String = "Competition"
}

@Serializable
data object LightenerRoute : AppRoute {
    override val screenName: String = "Lightener"
}

@Serializable
data object RoadmapRoute : AppRoute {
    override val screenName: String = "Roadmap"
}

@Serializable
data object ProfileRoute : AppRoute {
    override val screenName: String = "Profile"
}
// endregion

// region Detail / pushed screens (shared between outer and nested stacks)
@Serializable
data class SearchRoute(
    val categoryId: String? = null,
) : AppRoute {
    override val screenName: String = "Search"
}

/**
 * @param imageUrl nullable at the route level; normalized to "" when the screen
 * actually needs a non-null value (see AppNavigation). Kept nullable here so
 * callers can pass `scenario.imageUrl` directly without the `?: ""` noise.
 */
@Serializable
data class ScenarioDetailRoute(
    val id: String,
    val imageUrl: String?,
    val title: String,
    val isChallenge: Boolean,
) : AppRoute {
    override val screenName: String = "ScenarioDetail"
}

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
