package ir.aispeaking.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun TopGamingBar(
    modifier: Modifier = Modifier,
    level: Int = 3,
    currentXp: Int = 750,
    maxXp: Int = 1000,
    streakDays: Int = 5,
    gemsCount: Int = 120,
    onLevelClick: () -> Unit = {},
    onStreakClick: () -> Unit = {},
    onGemsClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. Level & XP Progress Pill (Left)
        LevelXpBadge(
            level = level,
            currentXp = currentXp,
            maxXp = maxXp,
            onClick = onLevelClick,
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Right side badges: Streak Flame + Gems Counter
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 2. Daily Streak Flame Badge
            StreakBadge(
                streakDays = streakDays,
                onClick = onStreakClick,
            )

            // 3. Gems / Diamonds Badge
            GemsBadge(
                gemsCount = gemsCount,
                onClick = onGemsClick,
            )
        }
    }
}

@Composable
fun LevelXpBadge(
    modifier: Modifier = Modifier,
    level: Int = 3,
    currentXp: Int = 750,
    maxXp: Int = 1000,
    onClick: () -> Unit = {},
) {
    val xpProgress = (currentXp.toFloat() / maxXp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(24.dp), spotColor = Color(0x60FFD54F))
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xE61E293B), Color(0xF20F172A)),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0x6638BDF8), Color(0x33334155)),
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // 100% Vector Golden Star Icon
            GameStarIcon(size = 24.dp)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Lv. $level",
                        color = Color(0xFFFFE082),
                        fontWeight = FontWeight.Bold,
                        fontFamily = AppTheme.typography.persianBold,
                        fontSize = 13.sp,
                    )

                    Text(
                        text = "$currentXp/$maxXp XP",
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Medium,
                        fontFamily = AppTheme.typography.persianRegular,
                        fontSize = 10.sp,
                    )
                }

                // Sleek glowing XP Progress bar
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF1E293B)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(xpProgress)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFB300),
                                        Color(0xFFFFD54F),
                                        Color(0xFF00E5FF),
                                    ),
                                ),
                            ),
                    )
                }
            }
        }
    }
}

@Composable
fun StreakBadge(
    modifier: Modifier = Modifier,
    streakDays: Int = 5,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x60FF9800))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xE61E293B), Color(0xF20F172A)),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFF9800), Color(0x66FF5722)),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 100% Vector Animated Flame Icon
            GameFlameIcon(size = 22.dp)

            Text(
                text = "$streakDays روز",
                color = Color(0xFFFFCC80),
                fontWeight = FontWeight.Bold,
                fontFamily = AppTheme.typography.persianBold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
fun GemsBadge(
    modifier: Modifier = Modifier,
    gemsCount: Int = 120,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x6000E5FF))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xE61E293B), Color(0xF20F172A)),
                ),
            )
            .border(
                width = 1.2.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF00E5FF), Color(0x550091EA)),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // 100% Vector Crystal Gem Icon
            GameGemIcon(size = 20.dp)

            Text(
                text = gemsCount.toString(),
                color = Color(0xFFE0F7FA),
                fontWeight = FontWeight.Bold,
                fontFamily = AppTheme.typography.persianBold,
                fontSize = 13.sp,
            )
        }
    }
}
