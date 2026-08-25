package ir.aispeaking.main.model

import ir.aispeaking.sharedui.challenge
import ir.aispeaking.sharedui.ic_challenge
import ir.aispeaking.sharedui.ic_challenge_active_dark
import ir.aispeaking.sharedui.ic_challenge_active_light
import ir.aispeaking.sharedui.ic_lightener
import ir.aispeaking.sharedui.ic_lightener_active_dark
import ir.aispeaking.sharedui.ic_lightener_active_light
import ir.aispeaking.sharedui.ic_profile
import ir.aispeaking.sharedui.ic_profile_active_dark
import ir.aispeaking.sharedui.ic_profile_active_light
import ir.aispeaking.sharedui.ic_roadmap
import ir.aispeaking.sharedui.ic_roadmap_active_dark
import ir.aispeaking.sharedui.ic_roadmap_active_light
import ir.aispeaking.sharedui.ic_scenario
import ir.aispeaking.sharedui.ic_scenario_active_dark
import ir.aispeaking.sharedui.ic_scenario_active_light
import ir.aispeaking.sharedui.lightener
import ir.aispeaking.sharedui.my_scores
import ir.aispeaking.sharedui.profile
import ir.aispeaking.sharedui.scenarios
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource


sealed class BottomBarItem(
    open val icon: DrawableResource,
    open val activeIconLight: DrawableResource,
    open val activeIconDark: DrawableResource,
    open val name: StringResource
) {
    data class Scenarios(
        override val icon: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_scenario,
        override val activeIconLight: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_scenario_active_light,
        override val activeIconDark: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_scenario_active_dark,
        override val name: StringResource = ir.aispeaking.sharedui.Res.string.scenarios
    ) : BottomBarItem(icon = icon, name = name, activeIconLight = activeIconLight, activeIconDark = activeIconDark)

    data class Profile(
        override val icon: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_profile,
        override val activeIconLight: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_profile_active_light,
        override val activeIconDark: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_profile_active_dark,
        override val name: StringResource = ir.aispeaking.sharedui.Res.string.profile
    ) : BottomBarItem(icon = icon, name = name, activeIconLight = activeIconLight, activeIconDark = activeIconDark)

    data class Challenge(
        override val icon: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_challenge,
        override val activeIconLight: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_challenge_active_light,
        override val activeIconDark: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_challenge_active_dark,
        override val name: StringResource = ir.aispeaking.sharedui.Res.string.challenge
    ) : BottomBarItem(icon = icon, name = name, activeIconLight = activeIconLight, activeIconDark = activeIconDark)

    data class Lightener(
        override val icon: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_lightener,
        override val activeIconLight: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_lightener_active_light,
        override val activeIconDark: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_lightener_active_dark,
        override val name: StringResource = ir.aispeaking.sharedui.Res.string.lightener
    ) : BottomBarItem(icon = icon, name = name, activeIconLight = activeIconLight, activeIconDark = activeIconDark)

    data class RoadMap(
        override val icon: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_roadmap,
        override val activeIconLight: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_roadmap_active_light,
        override val activeIconDark: DrawableResource = ir.aispeaking.sharedui.Res.drawable.ic_roadmap_active_dark,
        override val name: StringResource = ir.aispeaking.sharedui.Res.string.my_scores
    ) : BottomBarItem(icon = icon, name = name, activeIconLight = activeIconLight, activeIconDark = activeIconDark)

    companion object {
        val entries: List<BottomBarItem> = listOf(
            Profile(),
            Challenge(),
            Lightener(),
            RoadMap(),
            Scenarios(),
        )
    }
}
