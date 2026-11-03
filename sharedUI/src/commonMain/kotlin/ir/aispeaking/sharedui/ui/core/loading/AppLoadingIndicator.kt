package ir.aispeaking.sharedui.ui.core.loading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

/**
 * Unified application-wide loading spinner adhering to the Liquid Glass / Apple design system.
 * Features round stroke caps and an ethereal background track for consistent aesthetics across
 * all screens, buttons, and dialogs.
 */
@Composable
fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    strokeWidth: Dp = (size.value * 0.085f).dp.coerceIn(2.dp, 3.5.dp),
    color: Color = AppTheme.colors.primary,
    trackColor: Color = color.copy(alpha = 0.16f)
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        strokeWidth = strokeWidth,
        strokeCap = StrokeCap.Round,
        color = color,
        trackColor = trackColor
    )
}

/**
 * Centered full-screen or card loading state with unified indicator and optional status message.
 */
@Composable
fun AppLoadingView(
    modifier: Modifier = Modifier,
    text: String? = null,
    indicatorSize: Dp = 38.dp,
    color: Color = AppTheme.colors.primary,
    textColor: Color = Game.TextSecondary
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppLoadingIndicator(
                size = indicatorSize,
                color = color
            )
            if (!text.isNullOrBlank()) {
                GameText(
                    text = text,
                    size = 14.sp,
                    color = textColor
                )
            }
        }
    }
}
