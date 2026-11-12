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
    // In light theme, uses a high-opacity (92-94%) frosted surface to guarantee strong contrast against bright sky/cabin backdrops.
    val containerColor = if (isSubscriber) {
        // Subscribed: Elegant amethyst / violet frosted glass
        if (isDark) Color(0x4D6366F1) else Color(0xF0EEF2FF)
    } else {
        // Non-subscribed: Warm Crown Gold frosted glass (harmonized with VIP Crown)
        if (isDark) Color(0x38FFD60A) else Color(0xF2FFFBEB)
    }

    // Specular border gradient (hairline reflection on top, subtle tone at bottom)
    val borderBrush = if (isSubscriber) {
        Brush.verticalGradient(
            listOf(
                if (isDark) Color(0x99A5B4FC) else Color(0xFF818CF8), // Indigo / lavender top light
                if (isDark) Color(0x336366F1) else Color(0xFF6366F1), // Translucent violet bottom
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                if (isDark) Color(0xCCFFE885) else Color(0xFFF59E0B), // Warm gold specular top highlight
                if (isDark) Color(0x4DFFD60A) else Color(0xFFD97706), // Translucent amber bottom
            )
        )
    }

    // High contrast typography for maximum legibility in both light and dark modes
    val contentColor = if (isSubscriber) {
        if (isDark) Color(0xFFE0E7FF) else Color(0xFF312E81)
    } else {
        if (isDark) Color(0xFFFFFBEB) else Color(0xFF78350F)
    }

    val iconTint = if (isSubscriber) {
        if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)
    } else {
        if (isDark) Color(0xFFFFD60A) else Color(0xFFD97706)
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
