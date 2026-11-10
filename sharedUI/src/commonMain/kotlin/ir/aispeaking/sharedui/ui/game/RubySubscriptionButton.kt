package ir.aispeaking.sharedui.ui.game

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
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
 * Frosted Liquid Glass Button for VIP / Plus Subscription and Remaining Days HUD.
 *
 * Designed with a translucent glass aesthetic that subtly reveals the stage scenery artwork underneath.
 * Uses no rotating canvas sweep-gradients to avoid cross-browser rendering discrepancies on Web/Wasm.
 * Provides Apple-style tactile spring scale on press.
 */
@Composable
fun RubySubscriptionButton(
    text: String = "پلاس شو ✨",
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: DrawableResource = Res.drawable.ic_gem,
    isSubscriber: Boolean = false,
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

    val shape = RoundedCornerShape(22.dp)

    // Glass translucent backgrounds allowing the stage art underneath to be subtly visible
    val containerColor = if (isSubscriber) {
        // Subscribed: Elegant amethyst / violet translucent glass
        if (isDark) Color(0x3D6366F1) else Color(0x286366F1)
    } else {
        // Non-subscribed: Vibrant Ruby jewel translucent glass
        if (isDark) Color(0x3DE11D48) else Color(0x24E11D48)
    }

    // Specular border gradient (hairline reflection on top, subtle tone at bottom)
    val borderBrush = if (isSubscriber) {
        Brush.verticalGradient(
            listOf(
                Color(0x99A5B4FC), // Indigo / lavender top light
                Color(0x336366F1), // Translucent violet bottom
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color(0xB3FB7185), // Rose ruby top specular highlight
                Color(0x3BE11D48), // Translucent ruby bottom
            )
        )
    }

    val contentColor = if (isSubscriber) {
        if (isDark) Color(0xFFE0E7FF) else Color(0xFF3730A3)
    } else {
        if (isDark) Color(0xFFFFF1F2) else Color(0xFF881337)
    }

    val iconTint = if (isSubscriber) {
        if (isDark) Color(0xFFA5B4FC) else Color(0xFF6366F1)
    } else {
        if (isDark) Color(0xFFFB7185) else Color(0xFFE11D48)
    }

    Box(
        modifier = modifier
            .height(height)
            .defaultMinSize(minWidth = 110.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor)
            .border(1.2.dp, borderBrush, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // Subtle top specular shine inside the glass capsule
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to (if (isDark) Color(0x2BFFFFFF) else Color(0x40FFFFFF)),
                        0.55f to Color.Transparent
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            modifier = Modifier.padding(horizontal = 14.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(17.dp)
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
