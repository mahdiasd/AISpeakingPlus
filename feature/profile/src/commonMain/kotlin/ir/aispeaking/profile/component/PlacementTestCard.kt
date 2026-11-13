package ir.aispeaking.profile.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlacementTestCard(
    user: UserProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "placement_card_press"
    )

    val cardBg = if (isDark) Color(0x1AFFFFFF) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x24FFFFFF) else Color(0x1F000000)

    val levelText = user.languageLevel.takeIf { it.isNotBlank() } ?: "تعیین‌نشده"
    val subtitleText = if (user.languageLevel.isNotBlank()) {
        "سطح ثبت‌شده: $levelText • برای ارتقا دوباره آزمون دهید"
    } else {
        "سطح تسلط زبان انگلیسی خود را در ۵ دقیقه دقیق بسنجید"
    }

    val iconGradient = if (isDark) {
        listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
    } else {
        listOf(Color(0xFF0284C7), Color(0xFF4F46E5))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(22.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gradient Icon Badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(iconGradient)),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(
                        text = if (user.languageLevel.isNotBlank()) user.languageLevel else "🎯",
                        size = if (user.languageLevel.isNotBlank()) 18.sp else 20.sp,
                        bold = true,
                        color = Color.White
                    )
                }

                // Text Info
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameText(
                            text = "آزمون تعیین سطح انگلیسی",
                            size = 15.sp,
                            bold = true,
                            color = if (isDark) Game.TextPrimary else Color(0xFF0F172A)
                        )

                        // Highlight pill
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (isDark) Color(0x2238BDF8) else Color(0x1F0284C7))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            GameText(
                                text = "رایگان",
                                size = 10.sp,
                                bold = true,
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                            )
                        }
                    }

                    GameText(
                        text = subtitleText,
                        size = 12.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                    )
                }
            }

            // Chevron in RTL: rotation 180 to point left (forward direction in RTL)
            Icon(
                painter = painterResource(Res.drawable.ic_back),
                contentDescription = null,
                tint = if (isDark) Game.TextSecondary else Color(0xFF94A3B8),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayer { rotationZ = 180f }
            )
        }
    }
}
