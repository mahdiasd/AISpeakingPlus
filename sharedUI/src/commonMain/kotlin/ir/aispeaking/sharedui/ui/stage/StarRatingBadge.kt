package ir.aispeaking.sharedui.ui.stage

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun StarRatingBadge(
    stars: Int,
    modifier: Modifier = Modifier,
    starSize: TextUnit = 32.sp,
    spacing: Dp = 8.dp
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            val isEarned = index < stars
            val scale = remember { Animatable(0f) }

            LaunchedEffect(stars) {
                if (isEarned) {
                    delay(index * 200L)
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                } else {
                    scale.snapTo(1f)
                }
            }

            Text(
                text = if (isEarned) "⭐" else "☆",
                fontSize = starSize,
                color = if (isEarned) Color(0xFFFFD700) else Color(0xFF64748B),
                modifier = Modifier.scale(if (isEarned) scale.value else 1f)
            )
        }
    }
}
