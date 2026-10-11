package ir.aispeaking.profile.component

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.UserProfile
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_gamepad
import ir.aispeaking.sharedui.ic_gem
import ir.aispeaking.sharedui.ic_sound_wave
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_trend_up
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun AchievementStatsCard(
    user: UserProfile,
    modifier: Modifier = Modifier,
    onLevelClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header with Trend/Activity Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_trend_up),
                contentDescription = null,
                tint = Game.TextPrimary,
                modifier = Modifier.size(18.dp)
            )
            GameText(
                text = "آمار و پیشرفت یادگیری",
                color = Game.TextPrimary,
                size = 16.sp,
                bold = true
            )
        }

        // Row 1: Stars (Right) and Points/Score (Left) in RTL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "ستاره‌های دریافتی",
                value = user.totalStars.fa(),
                icon = Res.drawable.ic_star,
                iconTint = Game.Gold,
                iconBg = Color(0x26FFD60A),
                iconStroke = Color(0x4DFFD60A),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "امتیاز کل",
                value = user.score.fa(),
                icon = Res.drawable.ic_gem,
                iconTint = Game.Sky,
                iconBg = Color(0x2664D2FF),
                iconStroke = Color(0x4D64D2FF),
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: English Language Level (Right) and Completed Stages (Left) in RTL
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LevelStatCard(
                title = "سطح زبان انگلیسی",
                level = user.languageLevel.ifBlank { "A1" },
                icon = Res.drawable.ic_sound_wave,
                iconTint = Game.Violet,
                iconBg = Color(0x265E5CE6),
                iconStroke = Color(0x4D5E5CE6),
                onLevelClick = onLevelClick,
                modifier = Modifier.weight(1f)
            )

            StatCard(
                title = "مراحل تمام‌شده",
                value = user.completedStagesCount.fa(),
                icon = Res.drawable.ic_gamepad,
                iconTint = Game.Mint,
                iconBg = Color(0x2630D158),
                iconStroke = Color(0x4D30D158),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Metric card: Squircle icon at top-right, large stat value at top-left, title at bottom.
 */
@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: DrawableResource,
    iconTint: Color,
    iconBg: Color,
    iconStroke: Color,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .height(118.dp)
            .clip(shape)
            .background(Color(0xFF1B1C23))
            .border(1.dp, Color(0x22FFFFFF), shape)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Squircle icon on the right, large number on the left (in RTL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Squircle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg)
                        .border(1.dp, iconStroke, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Large Numeric Value
                GameText(
                    text = value,
                    color = Game.TextPrimary,
                    size = 22.sp,
                    bold = true
                )
            }

            // Bottom: Title
            GameText(
                text = title,
                color = Game.TextSecondary,
                size = 12.sp
            )
        }
    }
}

/**
 * Level Card: Sound waves icon at top-right, "تغییر" pill button at top-left,
 * with "سطح زبان انگلیسی" and bold level (e.g. "A2") at the bottom.
 */
@Composable
private fun LevelStatCard(
    title: String,
    level: String,
    icon: DrawableResource,
    iconTint: Color,
    iconBg: Color,
    iconStroke: Color,
    modifier: Modifier = Modifier,
    onLevelClick: (() -> Unit)? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onLevelClick != null) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "level_stat_press"
    )

    val shape = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .height(118.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(Color(0xFF1B1C23))
            .border(1.dp, if (onLevelClick != null) Color(0x385E5CE6) else Color(0x22FFFFFF), shape)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Sound wave icon on the right, "تغییر" action pill on the left (in RTL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Squircle
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBg)
                        .border(1.dp, iconStroke, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Change Action Pill
                if (onLevelClick != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0x2B5E5CE6))
                            .border(1.dp, Color(0x4D5E5CE6), RoundedCornerShape(10.dp))
                            .clickable(
                                interactionSource = interaction,
                                indication = null,
                                onClick = onLevelClick
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(
                            text = "تغییر",
                            color = Game.TextSecondary,
                            size = 11.sp,
                            bold = true
                        )
                    }
                }
            }

            // Bottom Row: "سطح زبان انگلیسی" on the right, Level (e.g. A2) on the left (in RTL)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameText(
                    text = title,
                    color = Game.TextSecondary,
                    size = 12.sp
                )

                GameText(
                    text = level,
                    color = Game.TextPrimary,
                    size = 19.sp,
                    bold = true
                )
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun AchievementStatsCardPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        AchievementStatsCard(
            user = UserProfile(
                id = "1",
                phoneNumber = "09123456789",
                nickName = "Ali",
                totalStars = 9,
                score = 0,
                completedStagesCount = 4,
                languageLevel = "A2"
            )
        )
    }
}
