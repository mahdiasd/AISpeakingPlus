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
import androidx.compose.foundation.layout.Spacer
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
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ic_roadmap
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_voice_model
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameText(
            text = "آمار و پیشرفت یادگیری",
            color = Game.TextSecondary,
            size = 13.sp,
            bold = true,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatItem(
                title = "ستاره‌های دریافتی",
                value = user.totalStars.fa(),
                icon = Res.drawable.ic_star,
                iconTint = Game.Gold,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                title = "امتیاز کل",
                value = user.score.fa(),
                icon = Res.drawable.ic_points,
                iconTint = Game.Sky,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatItem(
                title = "مراحل تمام‌شده",
                value = user.completedStagesCount.fa(),
                icon = Res.drawable.ic_roadmap,
                iconTint = Game.Mint,
                modifier = Modifier.weight(1f)
            )

            StatItem(
                title = "سطح زبان انگلیسی",
                value = user.languageLevel.ifBlank { "A1" },
                icon = Res.drawable.ic_voice_model,
                iconTint = Game.Violet,
                onClick = onLevelClick,
                badgeText = if (onLevelClick != null) "تغییر" else null,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: DrawableResource,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    badgeText: String? = null
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
        label = "stat_press"
    )

    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(Game.PanelRaised)
            .border(
                width = 1.dp,
                color = if (onClick != null) Game.Violet.copy(alpha = 0.45f) else Game.Stroke,
                shape = shape
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interaction,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Apple Squircle icon container
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.16f))
                        .border(1.dp, iconTint.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                badgeText?.let { badge ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Game.Violet.copy(alpha = 0.16f))
                            .border(1.dp, Game.Violet.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        GameText(
                            text = badge,
                            color = Game.Violet,
                            size = 11.sp,
                            bold = true
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                GameText(
                    text = value,
                    color = Game.TextPrimary,
                    size = 18.sp,
                    bold = true
                )
                GameText(
                    text = title,
                    color = Game.TextSecondary,
                    size = 11.sp
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
                totalStars = 15,
                score = 450,
                completedStagesCount = 5,
                languageLevel = "B1"
            )
        )
    }
}
