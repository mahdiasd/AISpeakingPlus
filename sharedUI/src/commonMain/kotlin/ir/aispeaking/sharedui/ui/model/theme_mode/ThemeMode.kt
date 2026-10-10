package ir.aispeaking.sharedui.ui.model.theme_mode


import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_theme_dark
import ir.aispeaking.sharedui.ic_theme_light
import ir.aispeaking.sharedui.ic_theme_system
import ir.aispeaking.sharedui.theme_dark_title
import ir.aispeaking.sharedui.theme_light_title
import ir.aispeaking.sharedui.theme_system_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

sealed class ThemeMode(
    val key: String,
    val icon: DrawableResource,
    val title: StringResource
) {
    data object System : ThemeMode(
        key = "System",
        icon = Res.drawable.ic_theme_system,
        title = Res.string.theme_system_title
    ) {
        operator fun invoke() = this
    }

    data object Dark : ThemeMode(
        key = "Dark",
        icon = Res.drawable.ic_theme_dark,
        title = Res.string.theme_dark_title
    ) {
        operator fun invoke() = this
    }

    data object Light : ThemeMode(
        key = "Light",
        icon = Res.drawable.ic_theme_light,
        title = Res.string.theme_light_title
    ) {
        operator fun invoke() = this
    }

    companion object {
        fun mapper(string: String): ThemeMode {
            return when (string) {
                System.key -> System
                Dark.key -> Dark
                Light.key -> Light
                else -> System
            }
        }
    }
}

