package ir.aispeaking.sharedui.ui.core.divider

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun HorizontalDashDivider(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.onSurface,
    thickness: Dp = 1.dp,
    dashLength: Float = 10f,
    dashGap: Float = 10f
) {
    Canvas(modifier = modifier.height(thickness)) {
        val canvasWidth = size.width
        val y = size.height / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, dashGap))
        )
    }
}