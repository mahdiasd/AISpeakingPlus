package ir.aispeaking.sharedui.ui.core.loading

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun DotLoading(
    modifier: Modifier = Modifier,
    count: Int = 3,
    dotColor: Color = AppTheme.colors.onPrimary,
    dotSize: Dp = 22.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        16.dp,
        alignment = Alignment.CenterHorizontally
    )
) {
    var currentIndex: Int by remember { mutableIntStateOf(0) }

    Row(
        Modifier
            .fillMaxWidth()
            .height(dotSize)
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = horizontalArrangement
    ) {
        repeat(count)
        { index ->
            Content(
                index = index,
                currentIndex = currentIndex,
                dotSize = dotSize,
                dotColor = dotColor,
                onFinish = {
                    if (currentIndex == count - 1) {
                        currentIndex = 0
                    } else currentIndex++
                }
            )
        }
    }
}

@Composable
private fun Content(
    index: Int,
    currentIndex: Int,
    dotSize: Dp,
    dotColor: Color,
    onFinish: () -> Unit
) {
    val isAnimate by remember(index, currentIndex) { mutableStateOf(currentIndex == index) }

    val animScale by animateFloatAsState(
        if (isAnimate) 1.5f else 1f, label = "",
        animationSpec = tween(750)
    )

    LaunchedEffect(animScale) {
        if (isAnimate && animScale > 1.4f) {
            onFinish()
        }
    }

    val animateColor by animateColorAsState(
        if (animScale > 1.2) dotColor else dotColor.copy(0.8f),
        label = ""
    )


    Canvas(
        modifier = Modifier
            .scale(animScale)
            .size(dotSize)
    ) {
        drawCircle(color = animateColor)
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    AppTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .background(
                    AppTheme.colors.primary,
                    shape = AppTheme.shapes.roundLarge
                )
                .height(50.dp),
            contentAlignment = Alignment.Center
        )
        {
            DotLoading()
        }
    }
}