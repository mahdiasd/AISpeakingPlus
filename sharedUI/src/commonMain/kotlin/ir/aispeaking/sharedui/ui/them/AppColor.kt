package ir.aispeaking.sharedui.ui.them

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColor(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceDim: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val outline: Color,
    val outlineVariant: Color,
    val error: Color,
    val onError: Color,
    val success: Color,
    val onSuccess: Color,
    val shadow: Color,
    val aiChatContainer: Color,
    val userChatContainer: Color,
    val points: Color,
    val roadMapSurface: Color,
)

val LocalColor = staticCompositionLocalOf<AppColor> { throw Exception("") }