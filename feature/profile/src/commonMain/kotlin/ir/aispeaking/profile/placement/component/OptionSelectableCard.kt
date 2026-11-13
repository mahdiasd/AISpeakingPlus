package ir.aispeaking.profile.placement.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun OptionSelectableCard(
    optionIndex: Int,
    optionText: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow),
        label = "option_press_scale"
    )

    val label = when (optionIndex) {
        0 -> "A"
        1 -> "B"
        2 -> "C"
        3 -> "D"
        else -> "${optionIndex + 1}"
    }

    // Dynamic Apple-style tints
    val activeBorderColor = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    val inactiveBorderColor = if (isDark) Color(0x24FFFFFF) else Color(0x1F000000)

    val activeBgColor = if (isDark) Color(0x2B0284C7) else Color(0x1A0284C7)
    val inactiveBgColor = if (isDark) Color(0x12FFFFFF) else Color(0xFFFFFFFF)

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) activeBgColor else inactiveBgColor,
        animationSpec = tween(150),
        label = "option_bg_anim"
    )

    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) activeBorderColor else inactiveBorderColor,
        animationSpec = tween(150),
        label = "option_border_anim"
    )

    val textColor = when {
        isSelected && isDark -> Color(0xFFF0F9FF)
        isSelected && !isDark -> Color(0xFF0369A1)
        isDark -> Game.TextPrimary
        else -> Color(0xFF1E293B)
    }

    val badgeBg = if (isSelected) {
        if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
    } else {
        if (isDark) Color(0x1FFFFFFF) else Color(0x12000000)
    }

    val badgeTextColor = if (isSelected) Color.White else (if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(animatedBg)
            .border(if (isSelected) 2.dp else 1.dp, animatedBorder, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onSelect
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // Enforce LTR inside the option row for proper English reading
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circle Badge (A, B, C, D)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeTextColor
                    )
                }

                // Option Text in English
                Text(
                    text = optionText,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = textColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
