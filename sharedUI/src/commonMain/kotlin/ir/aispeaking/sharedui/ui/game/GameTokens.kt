package ir.aispeaking.sharedui.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import ir.aispeaking.sharedui.ui.them.AppTheme

/**
 * Design tokens aligned with Apple Human Interface Guidelines & Emil Kowalski's craft sensibility.
 * Fully adaptive across Apple Light Mode and Dark Mode.
 *
 * Primary accent is Apple System Blue / Royal Azure (strictly NO green as primary).
 * Surfaces use Apple layered materials, translucent frosted glass in dark and crisp white cards in light.
 */
object Game {
    // Apple Surfaces & Materials
    val Ink: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF07080A) else Color(0xFFF2F4F7)

    val InkSoft: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF121316) else Color(0xFFE5E7EB)

    val Panel: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xD91C1C1E) else Color(0xD9FFFFFF)

    val PanelSolid: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xF21C1C1E) else Color(0xF2F8FAFC)

    val PanelRaised: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xE62C2C2E) else Color(0xE6FFFFFF)

    val Stroke: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x28FFFFFF) else Color(0x1F000000)

    val StrokeStrong: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x3DFFFFFF) else Color(0x2E000000)

    val SpecularStroke: Brush
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) {
            Brush.verticalGradient(listOf(Color(0x59FFFFFF), Color(0x1AFFFFFF)))
        } else {
            Brush.verticalGradient(listOf(Color(0x99FFFFFF), Color(0x1F000000)))
        }

    // Apple Typography Labels
    val TextPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFFFFFFFF) else Color(0xFF111318)

    val TextSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x99EBEBF5) else Color(0xFF6B7280)

    val TextMuted: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x4DEBEBF5) else Color(0xFF9CA3AF)

    // Apple System Accents - Primary is Apple System Blue
    val Blue: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

    // Green is prohibited as primary accent; Mint alias safely routes to Apple System Blue
    val Mint: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

    val MintDeep: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF0066CC) else Color(0xFF0056B3)

    val Gold: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFFFFD60A) else Color(0xFFB45309)

    val GoldDeep: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFFD4A000) else Color(0xFF92400E)

    val Violet: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF7D7AFF) else Color(0xFF5E5CE6)

    val VioletDeep = Color(0xFF4745B8)

    val Coral: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFFFF453A) else Color(0xFFFF3B30)

    val CoralDeep = Color(0xFFD70015)

    val Sky: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Chat Bubbles (Apple iMessage style)
    val BubbleAi: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF242426) else Color(0xFFF3F4F6)

    val BubbleUserTop: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

    val BubbleUserBottom: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF0071E3) else Color(0xFF0056B3)

    // Overlays & Scrims
    val ScrimTop: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xCC000000) else Color(0x29000000)

    val ScrimBottom: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xF2000000) else Color(0x66000000)

    val ModalScrim: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x8C000000) else Color(0x4D000000)

    val FallbackBackground: Brush
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) {
            Brush.verticalGradient(
                listOf(Color(0xFF181920), Color(0xFF0D0E12), Color(0xFF07080A))
            )
        } else {
            Brush.verticalGradient(
                listOf(Color(0xFFF9FAFB), Color(0xFFF2F4F7), Color(0xFFE5E7EB))
            )
        }
}
