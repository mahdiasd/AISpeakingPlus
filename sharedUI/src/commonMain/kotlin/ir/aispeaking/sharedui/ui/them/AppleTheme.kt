package ir.aispeaking.sharedui.ui.them

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
import androidx.compose.runtime.ReadOnlyComposable
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
 * 2026 Apple Liquid Glass Design System tokens & primitives.
 *
 * Adheres strictly to Apple Design Principles (WWDC 2026):
 * - Floating translucent materials with specular light rims.
 * - Tactile spring physics on all touch targets (damping 0.82, response 0.35s).
 * - Electric Indigo & Royal Azure primary accents.
 * - Crisp Serene Cyan for success / lookup highlight.
 * - Champagne Gold for hints and badges.
 * - Apple Coral for alerts.
 */
object AppleTheme {
    // Primary Accents (Electric Indigo & Royal Azure)
    val PrimaryAzure = Color(0xFF0A84FF)
    val PrimaryIndigo = Color(0xFF5E5CE6)
    val ElectricIndigo get() = PrimaryIndigo
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

    // Success / Serene Cyan
    val SuccessGreen = Color(0xFF30D158)
    val SuccessGreenLight = Color(0xFF34C759)
    val SuccessCyan = Color(0xFF64D2FF)
    val SuccessCyanDeep = Color(0xFF0284C7)
    val SuccessGlow = Color(0x3364D2FF)

    // Hints / Mission Badges (Warm Champagne Gold)
    val Gold: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFFFFD60A) else Color(0xFFB45309)

    val GoldDeep: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFFF59E0B) else Color(0xFF92400E)

    val GoldGlow: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x33FFD60A) else Color(0x26B45309)

    // Alerts / Errors (Apple Coral)
    val Coral = Color(0xFFFF453A)
    val CoralDeep = Color(0xFFD70015)
    val CoralGlow = Color(0x33FF453A)

    // Liquid Glass Materials
    val GlassPanel: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xD9161826) else Color(0xD9FFFFFF)

    val GlassPanelRaised: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xE6202438) else Color(0xE6FFFFFF)

    val GlassPanelElevated: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xF2272B42) else Color(0xF2F8FAFC)

    val GlassPanelSolid: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0xFF1C1F2E) else Color(0xF2F8FAFC)

    // Specular Light Rims
    val SpecularStrokeBrush: Brush
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) {
            Brush.verticalGradient(listOf(Color(0x59FFFFFF), Color(0x1AFFFFFF)))
        } else {
            Brush.verticalGradient(listOf(Color(0x99FFFFFF), Color(0x22000000)))
        }

    val SubtleStrokeBrush: Brush
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) {
            Brush.verticalGradient(listOf(Color(0x33FFFFFF), Color(0x0DFFFFFF)))
        } else {
            Brush.verticalGradient(listOf(Color(0x66FFFFFF), Color(0x14000000)))
        }

    val SpecularBorderColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x38FFFFFF) else Color(0x26000000)

    val SubtleBorderColor: Color
        @Composable
        @ReadOnlyComposable
        get() = if (AppTheme.isDark) Color(0x1FFFFFFF) else Color(0x14000000)
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

/**
 * Liquid Glass panel container with specular top rim and smooth corner squircle.
 */
@Composable
fun LiquidGlassPanel(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = AppleTheme.GlassPanel,
    borderBrush: Brush = AppleTheme.SpecularStrokeBrush,
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
