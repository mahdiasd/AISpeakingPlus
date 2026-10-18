package ir.aispeaking.chat.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 2026 Apple Liquid Glass Design System tokens & primitives for AISpeakingPlus Chat.
 *
 * Adheres strictly to Apple Design Principles (WWDC 2026):
 * - Floating translucent materials with specular light rims (1dp solid / gradient stroke).
 * - Tactile spring physics on all touch targets (damping 0.82, response 0.35s).
 * - Siri / Apple Intelligence iridescent glow for active voice / AI state.
 * - Electric Indigo & Royal Azure primary accents.
 * - Crisp Serene Cyan for success / grammar OK (strictly NO green).
 * - Champagne Gold for hints and mission tokens.
 * - Apple Coral for errors and cancel actions.
 */
object AppleChatTheme {
    // Primary Accents (Electric Indigo & Royal Azure)
    val PrimaryAzure = Color(0xFF0A84FF)
    val PrimaryIndigo = Color(0xFF5E5CE6)
    val AzureDeep = Color(0xFF0066CC)
    val IndigoDeep = Color(0xFF4845D2)

    val PrimaryAccentBrush = Brush.linearGradient(
        listOf(PrimaryAzure, PrimaryIndigo)
    )

    // Siri / Apple Intelligence Iridescent Glow
    val SiriViolet = Color(0xFF5E5CE6)
    val SiriCyan = Color(0xFF64D2FF)
    val SiriAzure = Color(0xFF0A84FF)
    val IridescentBrush = Brush.linearGradient(
        listOf(SiriViolet, SiriCyan, SiriAzure)
    )

    // Success / Grammar OK (Serene Cyan / Crisp Azure - strictly NO green)
    val SuccessCyan = Color(0xFF38BDF8)
    val SuccessCyanDeep = Color(0xFF0284C7)
    val SuccessGlow = Color(0x3338BDF8)

    // Hints / Mission Badges (Warm Champagne Gold)
    val Gold = Color(0xFFFFD60A)
    val GoldDeep = Color(0xFFF59E0B)
    val GoldGlow = Color(0x33FFD60A)

    // Alerts / Errors (Apple Coral)
    val Coral = Color(0xFFFF453A)
    val CoralDeep = Color(0xFFD70015)
    val CoralGlow = Color(0x33FF453A)

    // Liquid Glass Materials (visionOS & iOS 26 dark translucent surfaces)
    val GlassPanel = Color(0xD9161826)
    val GlassPanelRaised = Color(0xE6202438)
    val GlassPanelElevated = Color(0xF2272B42)
    val GlassPanelSolid = Color(0xFF1C1F2E)
    val BubbleAi = Color(0xD91D2030)

    // Specular Light Rims (simulates light refraction along glass edges)
    val SpecularStrokeBrush = Brush.verticalGradient(
        listOf(Color(0x59FFFFFF), Color(0x1AFFFFFF))
    )
    val SubtleStrokeBrush = Brush.verticalGradient(
        listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF))
    )
    val SpecularBorderColor = Color(0x38FFFFFF)
    val SubtleBorderColor = Color(0x1FFFFFFF)

    // User Bubble
    val UserBubbleBrush = Brush.linearGradient(
        listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
    )
}

/**
 * Liquid Glass panel container with specular top rim and smooth corner squircle.
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = AppleChatTheme.GlassPanel,
    borderBrush: Brush = AppleChatTheme.SpecularStrokeBrush,
    borderWidth: Dp = 1.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(borderWidth, borderBrush, shape),
        content = content
    )
}

/**
 * Apple tactile spring press modifier.
 * Scales element down smoothly to [targetScale] on press with critically tuned spring physics.
 */
@Composable
fun Modifier.applePressEffect(
    enabled: Boolean = true,
    targetScale: Float = 0.95f,
    onClick: (() -> Unit)? = null
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) targetScale else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "apple_press_scale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .then(
            if (onClick != null && enabled) {
                Modifier.clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onClick
                )
            } else Modifier
        )
}
