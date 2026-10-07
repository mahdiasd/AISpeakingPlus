package ir.aispeaking.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val navigationConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SplashRoute::class, SplashRoute.serializer())
            subclass(LoginRoute::class, LoginRoute.serializer())
            subclass(MainRoute::class, MainRoute.serializer())
            subclass(StageChatRoute::class, StageChatRoute.serializer())
            subclass(ProfileRoute::class, ProfileRoute.serializer())
            subclass(SubscriptionRoute::class, SubscriptionRoute.serializer())
            subclass(StagesListRoute::class, StagesListRoute.serializer())
        }
    }
}
