package ir.aispeaking.sharedui.ui.model.tab

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

interface UiTab {
    val title: StringResource

    val vector: DrawableResource?
}