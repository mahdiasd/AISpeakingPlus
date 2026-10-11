package ir.aispeaking.sharedui.ui.game

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Design tokens aligned with Apple Human Interface Guidelines & Emil Kowalski's craft sensibility.
 *
 * Uses Apple dark materials, translucent frosted glass layers, iOS system accents (Blue, Mint,
 * Indigo, Amber, Coral, Cyan), and iOS typography label hierarchies.
 */
object Game {
    // Apple Surfaces & Materials
    val Ink = Color(0xFF07080A)
    val InkSoft = Color(0xFF121316)
    val Panel = Color(0xD91C1C1E)
    val PanelSolid = Color(0xFF1C1C1E)
    val PanelRaised = Color(0xFF2C2C2E)
    val Stroke = Color(0x24FFFFFF)
    val StrokeStrong = Color(0x38FFFFFF)

    // Apple Typography Labels
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0x99EBEBF5) // 60% white (iOS secondary label)
    val TextMuted = Color(0x4DEBEBF5)     // 30% white (iOS tertiary label)

    // Apple System Accents
    val Blue = Color(0xFF0A84FF)
    val Mint = Color(0xFF30D158)
    val MintDeep = Color(0xFF248A3D)
    val Gold = Color(0xFFFFD60A)
    val GoldDeep = Color(0xFFD4A000)
    val Violet = Color(0xFF5E5CE6)
    val VioletDeep = Color(0xFF4745B8)
    val Coral = Color(0xFFFF453A)
    val CoralDeep = Color(0xFFD70015)
    val Sky = Color(0xFF64D2FF)

    // Chat Bubbles (Apple iMessage style)
    val BubbleAi = Color(0xFF242426)
    val BubbleUserTop = Color(0xFF0A84FF)
    val BubbleUserBottom = Color(0xFF0071E3)

    // Overlays & Scrims
    val ScrimTop = Color(0xCC000000)
    val ScrimBottom = Color(0xF2000000)
    val ModalScrim = Color(0x8C000000)

    val FallbackBackground = Brush.verticalGradient(
        listOf(Color(0xFF181920), Color(0xFF0D0E12), Color(0xFF07080A))
    )
}
