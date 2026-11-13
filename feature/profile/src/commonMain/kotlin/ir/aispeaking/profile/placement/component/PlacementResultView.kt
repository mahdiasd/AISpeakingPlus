package ir.aispeaking.profile.placement.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun PlacementResultView(
    result: PlacementEvaluationResult,
    onReviewClick: () -> Unit,
    onRetakeClick: () -> Unit,
    onFinishClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val cardBg = if (isDark) Color(0x1AFFFFFF) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000)

    val (badgeGradStart, badgeGradEnd) = when (result.assignedLevel.name) {
        "A1" -> Pair(Color(0xFF10B981), Color(0xFF059669))
        "A2" -> Pair(Color(0xFF06B6D4), Color(0xFF0284C7))
        "B1" -> Pair(Color(0xFF3B82F6), Color(0xFF1D4ED8))
        "B2" -> Pair(Color(0xFF6366F1), Color(0xFF4338CA))
        else -> Pair(Color(0xFFA855F7), Color(0xFF7E22CE))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Level Celebration Badge
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(badgeGradStart, badgeGradEnd))),
            contentAlignment = Alignment.Center
        ) {
            GameText(
                text = result.assignedLevel.name,
                size = 36.sp,
                bold = true,
                color = Color.White
            )
        }

        // Title and Description Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(26.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameText(
                    text = "سطح شما مشخص شد!",
                    size = 14.sp,
                    color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                )

                GameText(
                    text = result.levelTitleFa,
                    size = 18.sp,
                    bold = true,
                    color = if (isDark) Game.TextPrimary else Color(0xFF0F172A),
                    align = TextAlign.Center
                )

                GameText(
                    text = result.descriptionFa,
                    size = 13.sp,
                    lineHeight = 22.sp,
                    color = if (isDark) Game.TextSecondary else Color(0xFF475569),
                    align = TextAlign.Center
                )
            }
        }

        // Performance Statistics Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score Percent
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GameText(
                        text = "${result.scorePercent}٪",
                        size = 20.sp,
                        bold = true,
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                    )
                    GameText(
                        text = "نمره کل",
                        size = 12.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                    )
                }

                // Correct Count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GameText(
                        text = "${result.correctCount} از ${result.totalCount}",
                        size = 20.sp,
                        bold = true,
                        color = Color(0xFF10B981)
                    )
                    GameText(
                        text = "پاسخ‌های صحیح",
                        size = 12.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                    )
                }

                // Wrong Count
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GameText(
                        text = "${result.wrongCount}",
                        size = 20.sp,
                        bold = true,
                        color = Color(0xFFEF4444)
                    )
                    GameText(
                        text = "پاسخ‌های نادرست",
                        size = 12.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameButton(
                text = "مرور سوالات و پاسخ‌ها",
                onClick = onReviewClick,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameButton(
                    text = "آزمون مجدد",
                    style = GameButtonStyle.Glass,
                    onClick = onRetakeClick,
                    modifier = Modifier.weight(1f)
                )

                GameButton(
                    text = "ثبت و بازگشت",
                    onClick = onFinishClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
