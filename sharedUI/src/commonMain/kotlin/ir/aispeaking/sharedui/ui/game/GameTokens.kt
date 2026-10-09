package ir.aispeaking.sharedui.ui.game

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens for the game-style look of the learner app (not the admin panel).
 *
 * The palette is intentionally small: a deep-navy "ink" base so stage artwork pops,
 * one warm reward color (gold), one action color (mint) and one secondary accent (violet).
 */
object Game {
    // Surfaces
    val Ink = Color(0xFF080D22)
    val InkSoft = Color(0xFF111833)
    val Panel = Color(0xE6101734)
    val PanelSolid = Color(0xFF131B3B)
    val PanelRaised = Color(0xFF1B2550)
    val Stroke = Color(0x24FFFFFF)
    val StrokeStrong = Color(0x40FFFFFF)

    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB7C0E0)
    val TextMuted = Color(0xFF7F8AB3)

    // Action / reward / accent
    val Mint = Color(0xFF2EE6A8)
    val MintDeep = Color(0xFF12A878)
    val Gold = Color(0xFFFFC83D)
    val GoldDeep = Color(0xFFD98E00)
    val Violet = Color(0xFF8C6CFF)
    val VioletDeep = Color(0xFF5636D8)
    val Coral = Color(0xFFFF6B6B)
    val CoralDeep = Color(0xFFC53B3B)
    val Sky = Color(0xFF4CC9F0)

    // Chat
    val BubbleAi = Color(0xE6121A3A)
    val BubbleUserTop = Color(0xFF7C5CFF)
    val BubbleUserBottom = Color(0xFF5A3EE0)

    val ScrimTop = Color(0xCC050816)
    val ScrimBottom = Color(0xF2050816)

    val ModalScrim = Color(0xB3030612)

    val FallbackBackground = Brush.verticalGradient(
        listOf(Color(0xFF1B1F4B), Color(0xFF0E1433), Color(0xFF080D22))
    )
}
