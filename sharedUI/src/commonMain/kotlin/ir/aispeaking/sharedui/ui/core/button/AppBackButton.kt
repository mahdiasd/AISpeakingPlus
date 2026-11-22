package ir.aispeaking.sharedui.ui.core.button

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Standard circular top-bar icon button matching unified Apple sleek styling.
 * Automatically adapts to light/dark mode for crisp contrast and fluid tactile feedback.
 */
@Composable
fun AppTopBarIconButton(
    icon: DrawableResource,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    tint: Color = if (AppTheme.isDark) Color.White else Color(0xFF0F172A),
    containerSize: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    backgroundColor: Color = if (AppTheme.isDark) Color(0x33FFFFFF) else Color(0xCCFFFFFF),
    borderColor: Color = if (AppTheme.isDark) Color(0x38FFFFFF) else Color(0x1F000000)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "topBarBtnScale"
    )

    Box(
        modifier = modifier
            .size(containerSize)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(backgroundColor)
            .border(
                width = 1.5.dp,
                color = borderColor,
                shape = CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}

/**
 * Reusable Back Button with uniform container and right-pointing chevron for RTL.
 * Fully theme-aware: guarantees crisp contrast in both Light and Dark themes.
 */
@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = if (AppTheme.isDark) Color.White else Color(0xFF0F172A),
    containerSize: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    backgroundColor: Color = if (AppTheme.isDark) Color(0x33FFFFFF) else Color(0xCCFFFFFF),
    borderColor: Color = if (AppTheme.isDark) Color(0x38FFFFFF) else Color(0x1F000000)
) {
    AppTopBarIconButton(
        icon = Res.drawable.ic_back,
        onClick = onClick,
        contentDescription = "بازگشت",
        tint = tint,
        containerSize = containerSize,
        iconSize = iconSize,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        modifier = modifier
    )
}
