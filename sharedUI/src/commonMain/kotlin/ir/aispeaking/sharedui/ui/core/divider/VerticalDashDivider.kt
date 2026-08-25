package ir.aispeaking.sharedui.ui.core.divider

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalDashDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    thickness: Dp = 1.dp,
    dashLength: Float = 10f,
    dashGap: Float = 10f
) {
    Canvas(modifier = modifier.width(thickness)) {
        val canvasHeight = size.height
        val x = size.width / 2
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, canvasHeight),
            strokeWidth = thickness.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, dashGap))
        )
    }
}