package ir.aispeaking.stageslist.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_gem
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Apple-grade frosted glass header card summarizing journey progress,
 * collectible stars, and current season branding.
 */
@Composable
fun StagesHeaderStatsCard(
    totalStars: Int,
    maxStars: Int,
    completedStages: Int,
    totalStages: Int,
    progressPercentage: Float,
    modifier: Modifier = Modifier,
    seasonTitle: String = "فصل اول: روزهای آغازین در لندن"
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(26.dp)

    val surfaceBg = if (isDark) Color(0xD9171A27) else Color(0xEBFFFFFF)
    val specularStroke = Brush.verticalGradient(
        if (isDark) {
            listOf(Color(0x73FFFFFF), Color(0x18FFFFFF))
        } else {
            listOf(Color(0xB3FFFFFF), Color(0x24000000))
        }
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "headerProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(surfaceBg)
            .border(1.2.dp, specularStroke, shape)
    ) {
        // Atmospheric specular glow at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            if (isDark) Game.Blue.copy(alpha = 0.12f) else Game.Blue.copy(alpha = 0.06f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Row: Season Badge + Star Collectible Capsule
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Season Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Game.Gold.copy(alpha = if (isDark) 0.16f else 0.12f))
                        .border(1.dp, Game.Gold.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_gem),
                        contentDescription = null,
                        tint = Game.Gold,
                        modifier = Modifier.size(13.dp)
                    )
                    GameText(
                        text = seasonTitle,
                        size = 12.sp,
                        bold = true,
                        color = Game.Gold
                    )
                }

                // Collectible Stars Capsule
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Game.Gold.copy(alpha = if (isDark) 0.22f else 0.16f))
                        .border(1.dp, Game.Gold.copy(alpha = 0.55f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_star),
                        contentDescription = null,
                        tint = Game.Gold,
                        modifier = Modifier.size(15.dp)
                    )
                    GameText(
                        text = "${totalStars.fa()} / ${maxStars.fa()}",
                        size = 13.sp,
                        bold = true,
                        color = Game.Gold
                    )
                }
            }

            // Middle: Progress Numbers + Percentage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    GameText(
                        text = "پیشرفت ماجراجویی مکالمه",
                        size = 12.sp,
                        color = Game.TextSecondary
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GameText(
                            text = completedStages.fa(),
                            size = 24.sp,
                            bold = true,
                            color = Game.TextPrimary
                        )
                        GameText(
                            text = "از ${totalStages.fa()} مرحله تکمیل شده",
                            size = 14.sp,
                            bold = true,
                            color = Game.TextSecondary,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }

                // Apple-style Percentage Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Game.Blue.copy(alpha = if (isDark) 0.22f else 0.12f))
                        .border(1.dp, Game.Blue.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GameText(
                        text = "٪${(progressPercentage * 100).toInt().fa()}",
                        size = 14.sp,
                        bold = true,
                        color = Game.Blue
                    )
                }
            }

            // Apple Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Game.Blue,
                                    Game.Sky,
                                    Game.Gold
                                )
                            )
                        )
                )
            }
        }
    }
}
