package ir.aispeaking.main.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_profile
import ir.aispeaking.sharedui.ic_word
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun GamifiedArcBottomDock(
    modifier: Modifier = Modifier,
    activeLevelTitle: String = "مرحله ۴",
    onProfileClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onMainPlayClick: () -> Unit = {},
    onLightenerClick: () -> Unit = {},
    onStoreClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 12.dp, start = 10.dp, end = 10.dp),
        contentAlignment = Alignment.BottomCenter,
    ) {
        // 1. Curved Glassmorphism Dock Base
        ArcDockBackground(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp),
        )

        // 2. Arc Action Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.Bottom,
        ) {
            // Button 1: Profile (Far Left)
            VolumetricGameButton(
                modifier = Modifier.offset(y = (-4).dp),
                icon = Res.drawable.ic_profile,
                label = "پروفایل",
                accentColor = Color(0xFF38BDF8),
                onClick = onProfileClick,
            )

            // Button 2: Leaderboard (Mid Left)
            VolumetricGameButton(
                modifier = Modifier.offset(y = (-14).dp),
                icon = Res.drawable.ic_crown,
                label = "برترین‌ها",
                accentColor = Color(0xFFFFD54F),
                onClick = onLeaderboardClick,
            )

            // Button 3: Main Play CTA (CENTER - Elevated & 1.4x scale)
            VolumetricMainCtaButton(
                modifier = Modifier.offset(y = (-28).dp),
                activeLevelTitle = activeLevelTitle,
                onClick = onMainPlayClick,
            )

            // Button 4: Lightener / Vocabulary Cards (Mid Right)
            VolumetricGameButton(
                modifier = Modifier.offset(y = (-14).dp),
                icon = Res.drawable.ic_word,
                label = "جعبه لغات",
                accentColor = Color(0xFF64FFDA),
                onClick = onLightenerClick,
            )

            // Button 5: Subscription / VIP Store (Far Right)
            VolumetricVipButton(
                modifier = Modifier.offset(y = (-4).dp),
                label = "اشتراک ویژه",
                accentColor = Color(0xFFE040FB),
                onClick = onStoreClick,
            )
        }
    }
}

@Composable
private fun ArcDockBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val arcPath = Path().apply {
            moveTo(0f, height)
            lineTo(0f, height * 0.38f)
            quadraticTo(
                width * 0.5f, -14f,
                width, height * 0.38f,
            )
            lineTo(width, height)
            close()
        }

        // Drop shadow & glass body
        drawPath(
            path = arcPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xF20F172A),
                    Color(0xFA020617),
                ),
            ),
        )

        // Glowing Top Arc Bevel Line
        val strokePath = Path().apply {
            moveTo(0f, height * 0.38f)
            quadraticTo(
                width * 0.5f, -14f,
                width, height * 0.38f,
            )
        }

        drawPath(
            path = strokePath,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0x3338BDF8),
                    Color(0x9900E5FF),
                    Color(0xFFFFD54F),
                    Color(0x9900E5FF),
                    Color(0x3338BDF8),
                ),
            ),
            style = Stroke(width = 2f),
        )
    }
}

@Composable
fun VolumetricGameButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    label: String,
    accentColor: Color,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.92f else 1f

    Column(
        modifier = modifier
            .scale(pressScale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 3D Volumetric Squircle Button with tactile bottom bevel
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = accentColor.copy(alpha = 0.5f),
                    ambientColor = Color.Black,
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2A424E), // Light top bevel
                            Color(0xFF162730), // Mid body
                            Color(0xFF0C161C), // Deep bottom base
                        ),
                    ),
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.9f),
                            Color(0x40FFFFFF),
                            Color(0x30000000),
                        ),
                    ),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                colorFilter = ColorFilter.tint(Color(0xFFF1F5F9)),
            )
        }

        // Persian Label with Vazir Bold Font
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontFamily = AppTheme.typography.persianBold,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                textDirection = TextDirection.Rtl,
            ),
        )
    }
}

@Composable
fun VolumetricVipButton(
    modifier: Modifier = Modifier,
    label: String,
    accentColor: Color,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale = if (isPressed) 0.92f else 1f

    Column(
        modifier = modifier
            .scale(pressScale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 3D Volumetric VIP Button with crystal gem
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = accentColor.copy(alpha = 0.5f),
                    ambientColor = Color.Black,
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF382348), // Purple top bevel
                            Color(0xFF20132B), // Body
                            Color(0xFF110817), // Deep base
                        ),
                    ),
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.9f),
                            Color(0x40FFFFFF),
                            Color(0x30000000),
                        ),
                    ),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            GameGemIcon(size = 22.dp)
        }

        // Persian Label with Vazir Bold Font
        Text(
            text = label,
            color = Color(0xFFE2E8F0),
            fontSize = 11.sp,
            fontFamily = AppTheme.typography.persianBold,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                textDirection = TextDirection.Rtl,
            ),
        )
    }
}

@Composable
fun VolumetricMainCtaButton(
    modifier: Modifier = Modifier,
    activeLevelTitle: String = "مرحله ۴",
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "MainCtaTransitions")

    // Breathing pulse loop
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.055f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "CtaPulse",
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "CtaGlow",
    )

    val combinedScale = (if (isPressed) 0.93f else 1f) * pulseScale

    Column(
        modifier = modifier
            .scale(combinedScale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Outer Glowing Aura Box
        Box(
            modifier = Modifier
                .size(74.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Ambient Radial Golden Glow
            Canvas(modifier = Modifier.size(96.dp)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFB300).copy(alpha = 0.6f * glowAlpha),
                            Color(0xFFFF6F00).copy(alpha = 0.3f * glowAlpha),
                            Color.Transparent,
                        ),
                    ),
                    radius = size.width * 0.5f,
                )
            }

            // Glossy Volumetric Fiery Golden Button with 3D tactile edge
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .shadow(
                        elevation = 18.dp,
                        shape = RoundedCornerShape(22.dp),
                        spotColor = Color(0xFFFF8F00),
                        ambientColor = Color.Black,
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFF9C4), // Top shine
                                Color(0xFFFFCA28), // Bright gold
                                Color(0xFFFF8F00), // Fiery orange
                                Color(0xFFD84315), // Deep tactile base
                            ),
                        ),
                    )
                    .border(
                        width = 2.2.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFFFE082),
                                Color(0xFFFF6F00),
                            ),
                        ),
                        shape = RoundedCornerShape(22.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                // Play Icon
                Image(
                    painter = painterResource(Res.drawable.ic_play),
                    contentDescription = "Continue Adventure",
                    modifier = Modifier.size(36.dp),
                    colorFilter = ColorFilter.tint(Color.White),
                )
            }
        }

        // Bold Action Label with Persian Vazir Font
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = "ادامه ماجراجویی",
                color = Color(0xFFFFE082),
                fontSize = 12.sp,
                fontFamily = AppTheme.typography.persianBold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    textDirection = TextDirection.Rtl,
                ),
            )

            Text(
                text = "($activeLevelTitle)",
                color = Color(0xFFFFD54F),
                fontSize = 11.sp,
                fontFamily = AppTheme.typography.persianBold,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    textDirection = TextDirection.Rtl,
                ),
            )
        }
    }
}
