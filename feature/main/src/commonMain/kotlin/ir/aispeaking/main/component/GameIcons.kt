package ir.aispeaking.main.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 100% Vector 5-Point Golden Star with 3D gradient and specular shine.
 * Renders identically across all CMP targets (Android, JVM, iOS, WasmJs) without emoji dependencies.
 */
@Composable
fun GameStarIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f
        val cy = h * 0.52f
        val outerR = w * 0.46f
        val innerR = outerR * 0.44f

        val starPath = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i.toFloat() * (PI.toFloat() / 5f) - (PI.toFloat() / 2f))
            val x = cx + r * cos(angle)
            val y = cy + r * sin(angle)
            if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
        }
        starPath.close()


        // Outer soft glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x80FFD54F), Color.Transparent),
                center = Offset(cx, cy),
                radius = outerR * 1.3f,
            ),
            radius = outerR * 1.3f,
            center = Offset(cx, cy),
        )

        // Drop shadow
        drawPath(
            path = starPath,
            color = Color(0x66000000),
        )

        // Star body with gold gradient
        drawPath(
            path = starPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF9C4), // Highlight yellow
                    Color(0xFFFFD54F), // Bright gold
                    Color(0xFFFFB300), // Amber
                    Color(0xFFFF8F00), // Deep gold
                ),
            ),
        )

        // Star outline border
        drawPath(
            path = starPath,
            color = Color(0xFFFFFDE7),
            style = Stroke(
                width = 1.6f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round,
            ),
        )

        // Specular highlight on top point
        val highlightPath = Path().apply {
            moveTo(cx, cy - outerR)
            lineTo(cx + innerR * 0.5f, cy - innerR * 0.3f)
            lineTo(cx, cy)
            close()
        }
        drawPath(
            path = highlightPath,
            color = Color(0x70FFFFFF),
        )
    }
}

/**
 * 100% Vector Fiery Flame with animated flicker, multi-layer fire gradient, and glowing core.
 */
@Composable
fun GameFlameIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    animated: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "FlameIconTransition")
    val flickerScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "FlameFlicker",
        )
    } else {
        remember { androidx.compose.runtime.mutableStateOf(1f) }
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width * flickerScale
        val h = this.size.height * flickerScale
        val offsetX = (this.size.width - w) / 2f
        val offsetY = (this.size.height - h) / 2f

        // Outer Flame Body
        val outerFlame = Path().apply {
            moveTo(offsetX + w * 0.5f, offsetY + h * 0.05f)
            cubicTo(
                offsetX + w * 0.72f, offsetY + h * 0.28f,
                offsetX + w * 0.92f, offsetY + h * 0.55f,
                offsetX + w * 0.85f, offsetY + h * 0.78f,
            )
            cubicTo(
                offsetX + w * 0.78f, offsetY + h * 0.96f,
                offsetX + w * 0.22f, offsetY + h * 0.96f,
                offsetX + w * 0.15f, offsetY + h * 0.78f,
            )
            cubicTo(
                offsetX + w * 0.08f, offsetY + h * 0.58f,
                offsetX + w * 0.28f, offsetY + h * 0.38f,
                offsetX + w * 0.38f, offsetY + h * 0.48f,
            )
            cubicTo(
                offsetX + w * 0.32f, offsetY + h * 0.3f,
                offsetX + w * 0.42f, offsetY + h * 0.15f,
                offsetX + w * 0.5f, offsetY + h * 0.05f,
            )
            close()
        }

        // Ambient Fire Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x88FF6D00), Color(0x33FF9100), Color.Transparent),
                center = Offset(this.size.width * 0.5f, this.size.height * 0.65f),
                radius = this.size.width * 0.65f,
            ),
            radius = this.size.width * 0.65f,
            center = Offset(this.size.width * 0.5f, this.size.height * 0.65f),
        )

        // Outer Fire Gradient Fill
        drawPath(
            path = outerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFD54F), // Yellow top
                    Color(0xFFFF9800), // Orange
                    Color(0xFFFF5722), // Red-orange
                    Color(0xFFD84315), // Deep red base
                ),
            ),
        )

        // Inner Bright Core Flame
        val innerFlame = Path().apply {
            moveTo(offsetX + w * 0.5f, offsetY + h * 0.38f)
            cubicTo(
                offsetX + w * 0.68f, offsetY + h * 0.55f,
                offsetX + w * 0.72f, offsetY + h * 0.75f,
                offsetX + w * 0.62f, offsetY + h * 0.88f,
            )
            cubicTo(
                offsetX + w * 0.52f, offsetY + h * 0.94f,
                offsetX + w * 0.38f, offsetY + h * 0.88f,
                offsetX + w * 0.35f, offsetY + h * 0.75f,
            )
            cubicTo(
                offsetX + w * 0.32f, offsetY + h * 0.58f,
                offsetX + w * 0.44f, offsetY + h * 0.45f,
                offsetX + w * 0.5f, offsetY + h * 0.38f,
            )
            close()
        }

        drawPath(
            path = innerFlame,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFFFF9C4),
                    Color(0xFFFFEB3B),
                ),
            ),
        )
    }
}

/**
 * 100% Vector 3D Cyan Crystal Diamond Gem with faceted reflections.
 */
@Composable
fun GameGemIcon(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val pTopL = Offset(w * 0.22f, h * 0.22f)
        val pTopR = Offset(w * 0.78f, h * 0.22f)
        val pMidL = Offset(w * 0.05f, h * 0.46f)
        val pMidR = Offset(w * 0.95f, h * 0.46f)
        val pBottom = Offset(w * 0.5f, h * 0.92f)

        val pInTopL = Offset(w * 0.36f, h * 0.22f)
        val pInTopR = Offset(w * 0.64f, h * 0.22f)
        val pInMidL = Offset(w * 0.32f, h * 0.46f)
        val pInMidR = Offset(w * 0.68f, h * 0.46f)

        // Radial Cyan Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0x9900E5FF), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.5f),
                radius = w * 0.6f,
            ),
            radius = w * 0.6f,
            center = Offset(w * 0.5f, h * 0.5f),
        )

        // Facet 1: Center Crown (Bright highlight)
        val crownCenter = Path().apply {
            moveTo(pInTopL.x, pInTopL.y)
            lineTo(pInTopR.x, pInTopR.y)
            lineTo(pInMidR.x, pInMidR.y)
            lineTo(pInMidL.x, pInMidL.y)
            close()
        }
        drawPath(
            path = crownCenter,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA)),
            ),
        )

        // Facet 2: Left Crown
        val crownLeft = Path().apply {
            moveTo(pTopL.x, pTopL.y)
            lineTo(pInTopL.x, pInTopL.y)
            lineTo(pInMidL.x, pInMidL.y)
            lineTo(pMidL.x, pMidL.y)
            close()
        }
        drawPath(
            path = crownLeft,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFB2EBF2), Color(0xFF00E5FF)),
            ),
        )

        // Facet 3: Right Crown
        val crownRight = Path().apply {
            moveTo(pInTopR.x, pInTopR.y)
            lineTo(pTopR.x, pTopR.y)
            lineTo(pMidR.x, pMidR.y)
            lineTo(pInMidR.x, pInMidR.y)
            close()
        }
        drawPath(
            path = crownRight,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF80DEEA), Color(0xFF00B0FF)),
            ),
        )

        // Facet 4: Center Pavilion (Deep bottom triangle)
        val pavCenter = Path().apply {
            moveTo(pInMidL.x, pInMidL.y)
            lineTo(pInMidR.x, pInMidR.y)
            lineTo(pBottom.x, pBottom.y)
            close()
        }
        drawPath(
            path = pavCenter,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF00E5FF), Color(0xFF0288D1), Color(0xFF01579B)),
            ),
        )

        // Facet 5: Left Pavilion
        val pavLeft = Path().apply {
            moveTo(pMidL.x, pMidL.y)
            lineTo(pInMidL.x, pInMidL.y)
            lineTo(pBottom.x, pBottom.y)
            close()
        }
        drawPath(
            path = pavLeft,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF29B6F6), Color(0xFF0277BD)),
            ),
        )

        // Facet 6: Right Pavilion
        val pavRight = Path().apply {
            moveTo(pInMidR.x, pInMidR.y)
            lineTo(pMidR.x, pMidR.y)
            lineTo(pBottom.x, pBottom.y)
            close()
        }
        drawPath(
            path = pavRight,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0288D1), Color(0xFF01579B)),
            ),
        )

        // Diamond Outer Crisp Border
        val fullOutline = Path().apply {
            moveTo(pTopL.x, pTopL.y)
            lineTo(pTopR.x, pTopR.y)
            lineTo(pMidR.x, pMidR.y)
            lineTo(pBottom.x, pBottom.y)
            lineTo(pMidL.x, pMidL.y)
            close()
        }
        drawPath(
            path = fullOutline,
            color = Color(0xFFE0F7FA),
            style = Stroke(width = 1.4f, join = StrokeJoin.Round),
        )
    }
}

/**
 * 100% Vector Sound Speaker with 3 dynamic sound waves.
 */
@Composable
fun GameSpeakerIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SpeakerIconTransition")
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "WavePulse",
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Speaker Base Box + Cone
        val conePath = Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.32f, h * 0.35f)
            lineTo(w * 0.52f, h * 0.18f)
            lineTo(w * 0.52f, h * 0.82f)
            lineTo(w * 0.32f, h * 0.65f)
            lineTo(w * 0.15f, h * 0.65f)
            close()
        }
        drawPath(
            path = conePath,
            color = tint,
        )

        // Sound Wave 1 (Small)
        drawArc(
            color = tint.copy(alpha = 0.9f),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(w * 0.48f, h * 0.35f),
            size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.3f),
            style = Stroke(width = 2.2f, cap = StrokeCap.Round),
        )

        // Sound Wave 2 (Large)
        drawArc(
            color = tint.copy(alpha = waveAlpha),
            startAngle = -50f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(w * 0.55f, h * 0.22f),
            size = androidx.compose.ui.geometry.Size(w * 0.38f, h * 0.56f),
            style = Stroke(width = 2.2f, cap = StrokeCap.Round),
        )
    }
}
