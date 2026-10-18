package ir.aispeaking.profile.component

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_theme
import ir.aispeaking.sharedui.ic_theme_dark
import ir.aispeaking.sharedui.ic_theme_light
import ir.aispeaking.sharedui.ic_theme_system
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class ThemeOption(
    val mode: ThemeMode,
    val title: String,
    val icon: DrawableResource
)

/**
 * Apple 2026 Liquid Glass Theme Selection Card for Profile Screen.
 *
 * Implements Apple's HIG tactile 3-way segmented control (سیستم / تاریک / روشن)
 * with spring physics, specular highlight rims, and adaptive light/dark styling.
 */
@Composable
fun ThemeSelectionCard(
    currentThemeMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val cardShape = RoundedCornerShape(26.dp)

    val cardBg = if (isDark) Color(0xFF1B1C23) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x22FFFFFF) else Color(0x0F000000)

    val options = listOf(
        ThemeOption(ThemeMode.System, "سیستم", Res.drawable.ic_theme_system),
        ThemeOption(ThemeMode.Dark, "تاریک", Res.drawable.ic_theme_dark),
        ThemeOption(ThemeMode.Light, "روشن", Res.drawable.ic_theme_light)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (!isDark) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = cardShape,
                        spotColor = Color(0x0F000000),
                        ambientColor = Color(0x08000000)
                    )
                } else Modifier
            )
            .clip(cardShape)
            .background(cardBg)
            .border(1.dp, cardBorder, cardShape)
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Apple Icon badge + Title & Subtitle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val iconBg = if (isDark) Color(0x260A84FF) else Color(0x14007AFF)
                val iconTint = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(iconBg)
                        .border(
                            1.dp,
                            if (isDark) Color(0x400A84FF) else Color(0x26007AFF),
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_theme),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    GameText(
                        text = "ظاهر برنامه",
                        size = 16.sp,
                        bold = true,
                        color = if (isDark) Game.TextPrimary else Color(0xFF111318)
                    )
                    GameText(
                        text = "انتخاب حالت تیره، روشن یا هماهنگ با سیستم",
                        size = 12.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF6B7280)
                    )
                }
            }

            // Apple 3-Way Segmented Control Track
            val trackShape = RoundedCornerShape(16.dp)
            val trackBg = if (isDark) Color(0xFF14151B) else Color(0xFFEBEFF4)
            val trackBorder = if (isDark) Color(0x1AFFFFFF) else Color(0x0A000000)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(trackShape)
                    .background(trackBg)
                    .border(1.dp, trackBorder, trackShape)
                    .padding(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    options.forEach { option ->
                        val isSelected = currentThemeMode.key == option.mode.key

                        SegmentPill(
                            option = option,
                            isSelected = isSelected,
                            isDark = isDark,
                            onClick = { onThemeSelected(option.mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual Apple Segmented Pill with tactile spring press and active specular highlight.
 */
@Composable
private fun SegmentPill(
    option: ThemeOption,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "segment_press"
    )

    val pillShape = RoundedCornerShape(12.dp)

    val activeBg = if (isDark) Color(0xFF2C2D38) else Color(0xFFFFFFFF)
    val activeBorder = if (isDark) Color(0x38FFFFFF) else Color(0x0D000000)
    val activeTint = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF)

    val unselectedTint = if (isDark) Color(0x99EBEBF5) else Color(0xFF6B7280)

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) activeBg else Color.Transparent,
        animationSpec = tween(180),
        label = "segment_bg"
    )

    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) activeBorder else Color.Transparent,
        animationSpec = tween(180),
        label = "segment_border"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = if (isSelected) activeTint else unselectedTint,
        animationSpec = tween(180),
        label = "segment_content_color"
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isSelected && !isDark) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = pillShape,
                        spotColor = Color(0x1F000000),
                        ambientColor = Color(0x10000000)
                    )
                } else if (isSelected && isDark) {
                    Modifier.shadow(
                        elevation = 4.dp,
                        shape = pillShape,
                        spotColor = Color(0x40000000),
                        ambientColor = Color(0x20000000)
                    )
                } else Modifier
            )
            .clip(pillShape)
            .background(animatedBg)
            .border(1.dp, animatedBorder, pillShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(option.icon),
                contentDescription = option.title,
                tint = animatedContentColor,
                modifier = Modifier.size(16.dp)
            )

            GameText(
                text = option.title,
                size = 13.sp,
                bold = isSelected,
                color = animatedContentColor
            )
        }
    }
}
