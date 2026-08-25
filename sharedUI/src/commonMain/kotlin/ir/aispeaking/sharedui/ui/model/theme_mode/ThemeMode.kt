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
    open val key: String,
    open val icon: DrawableResource,
    open val title: StringResource
) {
    data class System(
        override val icon: DrawableResource = Res.drawable.ic_theme_system,
        override val title: StringResource = Res.string.theme_system_title
    ) : ThemeMode(key = "System", icon = icon, title = title)

    data class Dark(
        override val icon: DrawableResource = Res.drawable.ic_theme_dark,
        override val title: StringResource = Res.string.theme_dark_title
    ) : ThemeMode(key = "Dark", icon = icon, title = title)

    data class Light(
        override val icon: DrawableResource = Res.drawable.ic_theme_light,
        override val title: StringResource = Res.string.theme_light_title
    ) : ThemeMode(key = "Light", icon = icon, title = title)

    companion object {
        fun mapper(string: String): ThemeMode {
            return when (string) {
                System().key -> System()
                Dark().key -> Dark()
                Light().key -> Light()
                else -> System()
            }
        }
    }
}

