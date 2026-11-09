package ir.aispeaking.sharedui.ui.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_gem
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Animated glowing border button for VIP / Premium subscription.
 *
 * Implements a rotating gemstone beam (Amethyst -> Ruby -> Rose -> Diamond)
 * circulating along the rounded rectangular perimeter.
 * Adheres strictly to the no-yellow constraint, using a rich Ruby aesthetic.
 */
@Composable
fun RubySubscriptionButton(
    text: String = "اشتراک ویژه",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.ic_gem,
    height: Dp = 44.dp,
    enabled: Boolean = true,
) {
    val isDark = AppTheme.isDark
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    // Apple-style tactile spring scale on press
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "ruby_btn_press"
    )

    // Continuous orbiting beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "ruby_beam_loop")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "beam_orbit_angle"
    )

    // Gentle breathing pulse on the gem icon
    val gemScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ruby_gem_breathe"
    )

    val outerShape = RoundedCornerShape(16.dp)
    val innerShape = RoundedCornerShape(14.5.dp)

    val innerBg = if (isDark) {
        Brush.verticalGradient(
            listOf(
                Color(0xFF22111E), // Deep jewel obsidian top
                Color(0xFF130912), // Dark slate bottom
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xFFFFF2F5), // Soft pearl rose top
                Color(0xFFFFDFE7), // Pearl blush bottom
            )
        )
    }

    val contentColor = if (isDark) Color.White else Color(0xFF881337)
    val gemTint = if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)

    Box(
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Soft atmospheric aura glowing outside the border
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = 1.08f
                    scaleY = 1.20f
                    alpha = if (isDark) 0.32f else 0.18f
                }
        ) {
            rotate(degrees = angle, pivot = center) {
                drawCircle(
                    brush = Brush.sweepGradient(
                        0.00f to Color.Transparent,
                        0.60f to Color.Transparent,
                        0.74f to Color(0xFF6366F1), // Amethyst Violet
                        0.84f to Color(0xFFE11D48), // Ruby Red
                        0.93f to Color(0xFFFB7185), // Radiant Rose
                        0.97f to Color(0xFFFFFFFF), // Diamond Peak
                        1.00f to Color.Transparent,
                    ),
                    radius = size.maxDimension,
                    center = center
                )
            }
        }

        // 2. Button frame containing the masked circulating beam
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(outerShape)
                .background(if (isDark) Color(0x38E11D48) else Color(0x1FE11D48)),
            contentAlignment = Alignment.Center
        ) {
            // High-intensity circulating beam
            Canvas(modifier = Modifier.fillMaxSize()) {
                rotate(degrees = angle, pivot = center) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            0.00f to Color.Transparent,
                            0.56f to Color.Transparent,
                            0.68f to Color(0xFF6366F1), // Electric Indigo / Amethyst
                            0.79f to Color(0xFFC026D3), // Magenta Orchid
                            0.87f to Color(0xFFE11D48), // Rich Jewel Ruby
                            0.94f to Color(0xFFFB7185), // Bright Rose
                            0.98f to Color(0xFFFFFFFF), // Brilliant Diamond Sparkle
                            1.00f to Color.Transparent,
                        ),
                        radius = size.maxDimension,
                        center = center
                    )
                }
            }

            // 3. Inner Card (masks the center, exposing exactly the 1.5dp uniform border)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(1.5.dp)
                    .clip(innerShape)
                    .background(innerBg),
                contentAlignment = Alignment.Center
            ) {
                // Subtle top hairline specular shine
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to (if (isDark) Color(0x28FFFFFF) else Color(0x40FFFFFF)),
                                0.45f to Color.Transparent
                            )
                        )
                )

                // Label and animated Gem icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally),
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = gemTint,
                        modifier = Modifier
                            .size(18.dp)
                            .graphicsLayer {
                                scaleX = gemScale
                                scaleY = gemScale
                            }
                    )
                    GameText(
                        text = text,
                        size = 13.sp,
                        bold = true,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
