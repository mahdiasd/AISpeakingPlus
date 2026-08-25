package ir.aispeaking.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Saved-state configuration for the Navigation 3 back stack.
 *
 * Each [NavKey] subclass must be registered here so the back stack can be saved
 * and restored across configuration changes and process death. Non-JVM CMP
 * targets (JS/WasmJs/iOS) do not reflect over sealed hierarchies, so every
 * concrete route is registered explicitly.
 *
 * Keep this list in sync with [Routes.kt] when adding a new route.
 */
val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SplashRoute::class, SplashRoute.serializer())
            subclass(OnboardingRoute::class, OnboardingRoute.serializer())
            subclass(AuthRoute::class, AuthRoute.serializer())
            subclass(RegisterRoute::class, RegisterRoute.serializer())
            subclass(EnglishLevelRoute::class, EnglishLevelRoute.serializer())
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(ScenariosRoute::class, ScenariosRoute.serializer())
            subclass(CompetitionRoute::class, CompetitionRoute.serializer())
            subclass(LightenerRoute::class, LightenerRoute.serializer())
            subclass(RoadmapRoute::class, RoadmapRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(SearchRoute::class, SearchRoute.serializer())
            subclass(ScenarioDetailRoute::class, ScenarioDetailRoute.serializer())
            subclass(ChatRoute::class, ChatRoute.serializer())
            subclass(EditProfileRoute::class, EditProfileRoute.serializer())
            subclass(PurchasesRoute::class, PurchasesRoute.serializer())
        }
    }
}
