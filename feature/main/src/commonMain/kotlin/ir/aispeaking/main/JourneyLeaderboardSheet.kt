package ir.aispeaking.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.JourneyLeaderboard
import ir.aispeaking.domain.model.stage.LeaderboardEntry
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun JourneyLeaderboardSheet(
    leaderboard: JourneyLeaderboard,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    GameSheet(
        visible = true,
        onDismiss = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 جدول رده‌بندی مسافران",
                color = Game.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "بر اساس بیشترین ستاره‌ها و مراحل تکمیل‌شده مسیر داستانی",
                color = Game.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Current User Rank Card (if available)
            leaderboard.currentUserRank?.let { userRank ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF1E1B4B) else Color(0xD9EBF5FF)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDark) Color(0x336366F1) else Color(0x333B82F6)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Game.Blue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "#${userRank.rank}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("رتبه شما", color = Game.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(
                                    "${userRank.completedStages} مرحله تکمیل شده",
                                    color = if (isDark) Color(0xFFA5B4FC) else Game.Blue,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${userRank.totalStars}",
                                color = Game.Gold,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Top Learners List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(leaderboard.items) { entry ->
                    LeaderboardEntryRow(entry = entry)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LeaderboardEntryRow(
    entry: LeaderboardEntry
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Game.PanelRaised),
        border = androidx.compose.foundation.BorderStroke(1.dp, Game.Stroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Rank indicator
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (entry.rank) {
                        1 -> Text("🥇", fontSize = 20.sp)
                        2 -> Text("🥈", fontSize = 20.sp)
                        3 -> Text("🥉", fontSize = 20.sp)
                        else -> Text(
                            text = "${entry.rank}",
                            color = Game.TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = entry.displayName,
                        color = Game.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${entry.completedStages} مرحله",
                        color = Game.TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${entry.totalStars}",
                    color = Game.Gold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun JourneyLeaderboardSheetPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        JourneyLeaderboardSheet(
            leaderboard = JourneyLeaderboard(
                currentUserRank = LeaderboardEntry(
                    rank = 4,
                    userId = "user_me",
                    displayName = "Ali",
                    totalStars = 18,
                    completedStages = 6
                ),
                items = listOf(
                    LeaderboardEntry(
                        rank = 1,
                        userId = "u1",
                        displayName = "Sara",
                        totalStars = 30,
                        completedStages = 10
                    ),
                    LeaderboardEntry(
                        rank = 2,
                        userId = "u2",
                        displayName = "Reza",
                        totalStars = 27,
                        completedStages = 9
                    ),
                    LeaderboardEntry(
                        rank = 3,
                        userId = "u3",
                        displayName = "Maryam",
                        totalStars = 24,
                        completedStages = 8
                    )
                )
            ),
            onDismiss = {}
        )
    }
}
