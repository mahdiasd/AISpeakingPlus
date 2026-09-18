package ir.aispeaking.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute : NavKey {
    val screenName: String
}

@Serializable
data object MainRoute : AppRoute {
    override val screenName: String = "Main"
}
