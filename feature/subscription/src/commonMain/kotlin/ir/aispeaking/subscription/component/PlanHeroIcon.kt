package ir.aispeaking.subscription.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Modern stylized 3D abstract 4-petal flower icon matching the pricing card design.
 */
@Composable
fun PlanHeroIcon(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Ambient soft circular glow behind the flower
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.35f),
                            accentColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    )
                )
        )

        // 4-lobed smooth clover/flower shape
        Canvas(modifier = Modifier.size(44.dp)) {
            val centerOffset = this.center
            val petalRadius = size.minDimension * 0.24f
            val offsetDist = size.minDimension * 0.16f

            // 4 soft petals at 0, 90, 180, 270 degrees
            val angles = listOf(0.0, 90.0, 180.0, 270.0)
            for (angle in angles) {
                val rad = kotlin.math.PI * angle / 180.0
                val px = centerOffset.x + (offsetDist * kotlin.math.cos(rad)).toFloat()
                val py = centerOffset.y + (offsetDist * kotlin.math.sin(rad)).toFloat()

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.95f),
                            accentColor.copy(alpha = 0.90f),
                            accentColor.copy(alpha = 0.75f)
                        ),
                        center = Offset(px - petalRadius * 0.35f, py - petalRadius * 0.35f),
                        radius = petalRadius * 1.4f
                    ),
                    radius = petalRadius,
                    center = Offset(px, py)
                )
            }

            // Diagonal corner connectors for smooth clover blend
            val diagAngles = listOf(45.0, 135.0, 225.0, 315.0)
            for (angle in diagAngles) {
                val rad = kotlin.math.PI * angle / 180.0
                val px = centerOffset.x + (offsetDist * 0.75f * kotlin.math.cos(rad)).toFloat()
                val py = centerOffset.y + (offsetDist * 0.75f * kotlin.math.sin(rad)).toFloat()

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.85f),
                            accentColor.copy(alpha = 0.65f)
                        ),
                        center = Offset(px, py),
                        radius = petalRadius * 0.9f
                    ),
                    radius = petalRadius * 0.7f,
                    center = Offset(px, py)
                )
            }

            // Center radiant highlight core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.90f),
                        accentColor.copy(alpha = 0.80f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = petalRadius * 0.9f
                ),
                radius = petalRadius * 0.75f,
                center = centerOffset
            )
        }
    }
}
