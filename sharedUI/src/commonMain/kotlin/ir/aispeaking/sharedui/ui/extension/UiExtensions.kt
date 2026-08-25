package ir.aispeaking.sharedui.ui.extension


import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.them.AppTheme


fun Dp.toCorner() = CornerSize(this)

@Composable
fun LazyListState.OnBottomReached(
    loadMore: () -> Unit,
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount < 3) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.last()
                val viewportHeight = layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

                (lastVisibleItem.index + 1 == layoutInfo.totalItemsCount && lastVisibleItem.offset + lastVisibleItem.size <= viewportHeight)
            }
        }
    }
    // Convert the state into a cold flow and collect
    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .collect {
                // if should load more, then invoke loadMore
                if (it) loadMore()
            }
    }
}

@Composable
fun LazyGridState.OnBottomReached(
    loadMore: () -> Unit,
) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount < 3) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                lastVisibleItem?.let {
                    val viewportHeight =
                        layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset

                    // Check if the last visible item is the last item in the grid
                    // and if it's fully visible within the viewport
                    (it.index + 1 >= layoutInfo.totalItemsCount &&
                            it.offset.y + it.size.height <= viewportHeight)
                } ?: false
            }
        }
    }

    LaunchedEffect(shouldLoadMore) {
        snapshotFlow { shouldLoadMore.value }
            .collect { shouldLoad ->
                if (shouldLoad) loadMore()
            }
    }
}

@Composable
fun Dp.toComposePx(): Float {
    val density = LocalDensity.current.density
    return density * value
}

@Composable
fun Int.toComposeDp(): Dp {
    val density = LocalDensity.current.density
    return (this / density).dp
}


fun Modifier.baseModifier(padding: Dp = 0.dp): Modifier = composed {
    this
        .fillMaxSize()
        .background(AppTheme.colors.surface)
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(padding)
}

fun Modifier.iconSize(size: Dp = 24.dp): Modifier = composed {
    this.size(size)
}


fun Modifier.animateClickable(onClick: (() -> Unit)): Modifier = composed {
    this
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
}


fun String.toPrice(postPrice: String = "تومان"): String {
    if (this.isEmpty()) return "0"

    // Using Long to prevent overflow for large prices
    val number = this.toLongOrNull() ?: return ""

    // Reverse, chunk by 3, join with commas, and reverse back
    val formattedNumber = number.toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return "$formattedNumber $postPrice"
}

fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    shadowRadius: Dp = 20.dp,
    // Note: Native Material shadow uses a 3D lighting model,
    // so explicit X/Y offsets are handled automatically based on elevation.
    // These parameters are kept for compatibility with your existing usage.
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp,
): Modifier {
    // If alpha is 0 or radius is 0, skip the shadow calculation for better performance
    if (alpha == 0f || shadowRadius == 0.dp) return this

    val shadowColor = color.copy(alpha = alpha)

    // Using the native Compose shadow modifier with spotColor and ambientColor
    return this.shadow(
        elevation = shadowRadius,
        shape = RoundedCornerShape(borderRadius),
        spotColor = shadowColor,
        ambientColor = shadowColor,
        clip = false
    )
}



fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 1.dp,
    dashPattern: FloatArray = floatArrayOf(20f, 20f),
    cornerRadius: Dp = 0.dp,
): Modifier = this.then(
    drawBehind {
        val stroke = strokeWidth.toPx()
        val pathEffect = PathEffect.dashPathEffect(intervals = dashPattern, phase = 0f)

        val rect = Rect(
            offset = Offset(x = stroke / 2, y = stroke / 2),
            size = size.copy(width = size.width - stroke, height = size.height - stroke)
        )

        drawRoundRect(
            color = color,
            topLeft = rect.topLeft,
            size = rect.size,
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
            style = Stroke(width = stroke, pathEffect = pathEffect)
        )
    }
)

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    strokeWidth: Dp = 1.dp,
    dashLength: Float = 10f,
    dashGap: Float = 10f,
) {
    Canvas(modifier = modifier.height(strokeWidth)) {
        val canvasWidth = size.width
        val y = size.height / 2
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(canvasWidth, y),
            strokeWidth = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, dashGap))
        )
    }
}


@Composable
fun Modifier.animatedBorder(
    borderColors: List<Color>,
    backgroundColor: Color,
    shape: Shape = AppTheme.shapes.roundMedium,
    borderWidth: Dp,
    animationDurationInMillis: Int = 1000,
    easing: Easing = LinearEasing,
): Modifier {
    val brush = Brush.sweepGradient(borderColors)
    val infiniteTransition = rememberInfiniteTransition(label = "animatedBorder")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationInMillis, easing = easing),
            repeatMode = RepeatMode.Restart
        ), label = "angleAnimation"
    )

    return this
        .clip(shape)
        .padding(borderWidth)
        .drawWithContent {
            rotate(angle) {
                drawCircle(
                    brush = brush,
                    radius = size.width,
                    blendMode = BlendMode.SrcIn,
                )
            }
            drawContent()
        }
        .background(color = backgroundColor, shape = shape)
}

@Preview(
    name = "Light",
    uiMode = UI_MODE_NIGHT_NO,
    backgroundColor = 0xFFF5FAFD,
    showBackground = true
)
@Preview(
    name = "Dark",
    uiMode = UI_MODE_NIGHT_YES,
    backgroundColor = 0xFF0F1416,
    showBackground = true
)
annotation class LightDarkPreview


