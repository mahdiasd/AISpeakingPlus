package ir.aispeaking.main.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

private data class MagicParticle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val baseAlpha: Float,
    val speed: Float,
    val driftFactor: Float,
    val color: Color,
)

@Composable
fun AtmosphericFantasyBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AtmosphereTransition")

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "ParticleLoop",
    )

    val ambientGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AmbientGlowPulse",
    )

    val particles = remember {
        val random = Random(42)
        val particleColors = listOf(
            Color(0xFF00E5FF), // Magic Cyan
            Color(0xFF64FFDA), // Mint Turquoise
            Color(0xFFFFD54F), // Gold Dust
            Color(0xFF80DEEA), // Soft Aqua
            Color(0xFFFFF9C4), // Star Yellow
        )
        List(36) {
            MagicParticle(
                initialX = random.nextFloat(),
                initialY = random.nextFloat(),
                radius = random.nextFloat() * 3.5f + 1.5f,
                baseAlpha = random.nextFloat() * 0.45f + 0.25f,
                speed = random.nextFloat() * 0.4f + 0.15f,
                driftFactor = random.nextFloat() * 3f + 1f,
                color = particleColors[random.nextInt(particleColors.size)],
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF041216), // Dark Obsidian Teal (Top)
                        Color(0xFF081F26), // Deep Fantasy Teal
                        Color(0xFF0D2D35), // Mystic Emerald Core
                        Color(0xFF133F4A), // Rich Jungle Horizon
                        Color(0xFF0A2228), // Shadow Ground (Bottom)
                    ),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Top volumetric light cone / ambient sunbeam
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x3800E5FF),
                        Color(0x1800B0FF),
                        Color(0x0500695C),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.5f, height * 0.08f),
                    radius = width * 0.85f,
                ),
                radius = width * 0.85f,
                center = Offset(width * 0.5f, height * 0.08f),
            )

            // 2. Center stage ambient back-light
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0x2800E5FF).copy(alpha = 0.22f * ambientGlowPulse),
                        Color(0x1264FFDA).copy(alpha = 0.14f * ambientGlowPulse),
                        Color.Transparent,
                    ),
                    center = Offset(width * 0.5f, height * 0.52f),
                    radius = width * 0.7f,
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.5f, height * 0.52f),
            )

            // 3. Floating Magical Particles and Fireflies
            particles.forEach { particle ->
                val currentYFraction = (particle.initialY - (animationProgress * particle.speed)) % 1f
                val y = if (currentYFraction < 0f) (currentYFraction + 1f) * height else currentYFraction * height
                val waveOffset = sin((animationProgress * 6.28318f) + (particle.driftFactor * 3.14159f)) * (16f * particle.driftFactor)
                val x = (particle.initialX * width + waveOffset).coerceIn(0f, width)

                // Twinkle alpha calculation
                val twinkle = (sin((animationProgress * 12.56637f) + (particle.initialX * 10f)) + 1f) * 0.5f
                val dynamicAlpha = (particle.baseAlpha * (0.6f + 0.4f * twinkle)).coerceIn(0f, 1f)

                // Outer soft particle glow
                drawCircle(
                    color = particle.color.copy(alpha = dynamicAlpha * 0.35f),
                    radius = particle.radius * 2.6f,
                    center = Offset(x, y),
                )
                // Crisp bright core
                drawCircle(
                    color = Color.White.copy(alpha = dynamicAlpha),
                    radius = particle.radius * 0.85f,
                    center = Offset(x, y),
                )
            }
        }

        content()
    }
}
