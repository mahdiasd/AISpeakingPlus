package ir.aispeaking.main.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.avatar_b1
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.utils.avatar.AvatarUtils
import org.jetbrains.compose.resources.painterResource

@Composable
fun CenterStage(
    modifier: Modifier = Modifier,
    characterName: String = "ایندی",
    characterSubtitle: String = "کاوشگر سطح ۳",
    userAvatarName: String? = null,
    speechText: String = "سلام علی! آماده‌ای برای ماجراجویی مرحله ۴؟",
    onDialogueAudioClick: () -> Unit = {},
    onAvatarClick: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CenterStageTransitions")

    // Character subtle breathing & vertical floating loop
    val characterBobbing by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "CharacterBobbing",
    )

    val characterBreathScale by infiniteTransition.animateFloat(
        initialValue = 0.99f,
        targetValue = 1.025f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "CharacterBreathScale",
    )

    // Speech bubble subtle vertical bobbing
    val bubbleBobbing by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "BubbleBobbing",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 1. Dialogue Speech Bubble (Floating above avatar)
        DialogueSpeechBubble(
            modifier = Modifier
                .offset(y = bubbleBobbing.dp)
                .padding(horizontal = 20.dp),
            text = speechText,
            onAudioClick = onDialogueAudioClick,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Character & 3D Podium Combined Stage
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 270.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // 3D Stone & Moss Platform Podium
            PodiumPlatform(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp),
                bobbingOffset = characterBobbing,
            )

            // 3D Character Avatar on Stage
            CharacterAvatarStage(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (characterBobbing - 36).dp)
                    .scale(characterBreathScale),
                userAvatarName = userAvatarName,
                onClick = {
                    onAvatarClick()
                    onDialogueAudioClick()
                },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Character Identity Badge
        CharacterIdentityBadge(
            name = characterName,
            subtitle = characterSubtitle,
        )
    }
}

@Composable
fun DialogueSpeechBubble(
    modifier: Modifier = Modifier,
    text: String,
    onAudioClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Bubble Body Card
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color(0x6600E5FF),
                    ambientColor = Color(0x33000000),
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xF21E293B), // Dark Slate Glass
                            Color(0xF00F172A),
                        ),
                    ),
                )
                .border(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF38BDF8), Color(0x5500E5FF), Color(0x33FFFFFF)),
                    ),
                    shape = RoundedCornerShape(22.dp),
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = onAudioClick)
                .padding(horizontal = 18.dp, vertical = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Vector Animated Speaker Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF0288D1)),
                            ),
                        )
                        .border(1.dp, Color(0xFF80DEEA), CircleShape)
                        .clickable(onClick = onAudioClick),
                    contentAlignment = Alignment.Center,
                ) {
                    GameSpeakerIcon(size = 20.dp, tint = Color.White)
                }

                // Persian Speech Text with proper Vazir font & RTL
                Text(
                    text = text,
                    color = Color(0xFFF8FAFC),
                    fontSize = 14.sp,
                    fontFamily = AppTheme.typography.persianBold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = TextDirection.Rtl,
                    ),
                    lineHeight = 22.sp,
                )
            }
        }

        // Downward pointer tail
        SpeechBubbleTail(
            modifier = Modifier
                .size(width = 20.dp, height = 11.dp)
                .offset(y = (-1).dp),
        )
    }
}

@Composable
private fun SpeechBubbleTail(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width * 0.5f, size.height)
            close()
        }
        drawPath(
            path = path,
            color = Color(0xF00F172A),
        )
        drawPath(
            path = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width * 0.5f, size.height)
                lineTo(size.width, 0f)
            },
            color = Color(0x6638BDF8),
            style = Stroke(width = 1.6f),
        )
    }
}

@Composable
fun PodiumPlatform(
    modifier: Modifier = Modifier,
    bobbingOffset: Float = 0f,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PodiumRuneGlow")
    val runeGlowPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "RunePulse",
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerX = width * 0.5f
        val centerY = height * 0.48f
        val radiusX = width * 0.44f
        val radiusY = height * 0.32f

        // 1. Ambient Drop Shadow underneath the floating stone
        drawOval(
            color = Color.Black.copy(alpha = 0.55f),
            topLeft = Offset(centerX - radiusX * 0.95f, centerY + radiusY * 0.4f),
            size = Size(radiusX * 1.9f, radiusY * 1.5f),
        )

        // 2. Glowing Runic Aura Ring (Pulsating Emerald / Cyan)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF00E5FF).copy(alpha = 0.5f * runeGlowPulse),
                    Color(0xFF64FFDA).copy(alpha = 0.3f * runeGlowPulse),
                    Color.Transparent,
                ),
                center = Offset(centerX, centerY),
                radius = radiusX * 1.25f,
            ),
            topLeft = Offset(centerX - radiusX * 1.25f, centerY - radiusY * 1.25f),
            size = Size(radiusX * 2.5f, radiusY * 2.5f),
        )

        // 3. 3D Carved Stone Pedestal Lower Cylinder Base
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0F1E24), Color(0xFF070E12)),
            ),
            topLeft = Offset(centerX - radiusX, centerY + 14f),
            size = Size(radiusX * 2f, radiusY * 2f),
        )

        // 4. Stone Rim Bevel (Middle 3D Layer)
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1E3A45), Color(0xFF0F252D)),
            ),
            topLeft = Offset(centerX - radiusX, centerY + 6f),
            size = Size(radiusX * 2f, radiusY * 2f),
        )

        // 5. Stone Top Surface with Ancient Moss Accents
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFF2A5354), // Moss Green Highlight in Center
                    Color(0xFF1E3F47), // Ancient Carved Stone
                    Color(0xFF132A30), // Dark Rim
                ),
                center = Offset(centerX, centerY),
                radius = radiusX,
            ),
            topLeft = Offset(centerX - radiusX, centerY - radiusY),
            size = Size(radiusX * 2f, radiusY * 2f),
        )

        // 6. Glowing Rune Edge Inscription
        drawOval(
            color = Color(0xFF00E5FF).copy(alpha = 0.65f * runeGlowPulse),
            topLeft = Offset(centerX - radiusX * 0.92f, centerY - radiusY * 0.92f),
            size = Size(radiusX * 1.84f, radiusY * 1.84f),
            style = Stroke(width = 2.2f),
        )

        // 7. Character Dynamic Drop Shadow on Top Surface
        val shadowScale = (1f - (bobbingOffset / 30f)).coerceIn(0.7f, 1.3f)
        drawOval(
            color = Color(0x99000000),
            topLeft = Offset(centerX - (48f * shadowScale), centerY - (15f * shadowScale)),
            size = Size(96f * shadowScale, 30f * shadowScale),
        )
    }
}

@Composable
fun CharacterAvatarStage(
    modifier: Modifier = Modifier,
    userAvatarName: String? = null,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    val avatarProfile = AvatarUtils.findAvatarByNameComposable(userAvatarName)
    val avatarDrawable = avatarProfile?.drawable ?: Res.drawable.avatar_b1

    Box(
        modifier = modifier
            .size(185.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        // Glowing Ambient Backlight for Avatar
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0x5500E5FF),
                            Color(0x2264FFDA),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        // Stylized Character Frame with smooth bottom blend
        Box(
            modifier = Modifier
                .size(175.dp)
                .clip(CircleShape)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFFD54F),
                            Color(0xFF00E5FF),
                            Color(0xFF64FFDA),
                            Color(0xFFFFD54F),
                        ),
                    ),
                    shape = CircleShape,
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x330F2C35),
                            Color(0x88061418),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(avatarDrawable),
                contentDescription = "Character Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
            )
        }
    }
}

@Composable
fun CharacterIdentityBadge(
    modifier: Modifier = Modifier,
    name: String = "ایندی",
    subtitle: String = "کاوشگر سطح ۳",
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Character Name with Persian Font & Glow
        Text(
            text = name,
            color = Color.White,
            fontFamily = AppTheme.typography.persianBold,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(
                textDirection = TextDirection.Rtl,
            ),
        )

        // Character Subtitle Pill
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = Color(0x60FFD54F))
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xD90F172A), Color(0xF21E293B), Color(0xD90F172A)),
                    ),
                )
                .border(1.2.dp, Color(0x80FFD54F), RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 4.dp),
        ) {
            Text(
                text = subtitle,
                color = Color(0xFFFFE082),
                fontFamily = AppTheme.typography.persianBold,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                style = androidx.compose.ui.text.TextStyle(
                    textDirection = TextDirection.Rtl,
                ),
            )
        }
    }
}
