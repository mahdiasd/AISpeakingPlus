package ir.aispeaking.sharedui.ui.guide

import org.jetbrains.compose.resources.DrawableResource

enum class GuideAlignment {
    Auto,
    Top,
    Bottom
}

/**
 * Represents a single step in an in-app feature tour.
 */
data class GuideStep(
    val key: String,
    val title: String,
    val description: String,
    val icon: DrawableResource? = null,
    val alignment: GuideAlignment = GuideAlignment.Auto
)
