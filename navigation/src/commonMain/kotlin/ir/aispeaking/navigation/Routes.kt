package ir.aispeaking.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    val screenName: String
}

@Serializable
data object SplashRoute : AppRoute {
    override val screenName: String = "Splash"
}

@Serializable
data object LoginRoute : AppRoute {
    override val screenName: String = "Login"
}

@Serializable
data object MainRoute : AppRoute {
    override val screenName: String = "Main"
}

@Serializable
data class StageChatRoute(val stageId: String) : AppRoute {
    override val screenName: String = "StageChat"
}
