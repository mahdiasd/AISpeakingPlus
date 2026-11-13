package ir.aispeaking.profile.placement.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun PlacementProgressBar(
    currentIndex: Int,
    totalQuestions: Int,
    answeredCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val fraction = if (totalQuestions > 0) ((currentIndex + 1).toFloat() / totalQuestions).coerceIn(0.001f, 1f) else 0.001f
    val animatedProgress by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "placement_progress_anim"
    )

    val trackBg = if (isDark) Color(0x1AFFFFFF) else Color(0x1F000000)
    val gradientColors = if (isDark) {
        listOf(Color(0xFF38BDF8), Color(0xFF6366F1), Color(0xFFA855F7))
    } else {
        listOf(Color(0xFF0284C7), Color(0xFF4F46E5), Color(0xFF9333EA))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GameText(
                text = "سوال ${currentIndex + 1} از $totalQuestions",
                size = 14.sp,
                bold = true,
                color = if (isDark) Game.TextPrimary else Color(0xFF1E293B)
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x2210B981) else Color(0x1F10B981))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = "$answeredCount پاسخ‌داده‌شده",
                    size = 11.sp,
                    color = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
                )
            }
        }

        // Animated Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(trackBg)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress.coerceIn(0.001f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(gradientColors))
            )
        }
    }
}
