package ir.aispeaking.sharedui.ui.core.text

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.DrawModifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun AutoScrollingText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = AppTheme.typography.bodyMedium,
    durationMillis: Int = 5000,
    delayMillis: Int = 1000,
    edgeFadingLength: Dp = 16.dp
) {
    var parentWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember { mutableIntStateOf(0) }

    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }
    val shouldScroll = textWidth > parentWidth

    val infiniteTransition = rememberInfiniteTransition(label = "scroll")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldScroll) -(textWidth - parentWidth).toFloat() else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = LinearEasing,
                delayMillis = delayMillis
            ),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(delayMillis)
        ),
        label = "offset"
    )

    Box(
        modifier = modifier
            .clipToBounds()
            .onGloballyPositioned { coordinates ->
                parentWidth = coordinates.size.width
            }
    ) {
        Text(
            text = text,
            style = style,
            modifier = Modifier
                .graphicsLayer { translationX = animatedOffset }
                .onTextLayout { textLayoutResult ->
                    textLayoutResultState.value = textLayoutResult
                    textWidth = textLayoutResult.size.width
                },
            maxLines = 1,
            color = AppTheme.colors.onSurface,
            overflow = TextOverflow.Visible
        )

        // Gradient edges for better visual effect
        if (shouldScroll) {
            Row(modifier = Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .width(edgeFadingLength)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    AppTheme.colors.onSurface,
                                    Color.Transparent
                                )
                            )
                        )
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(edgeFadingLength)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    AppTheme.colors.onSurface
                                )
                            )
                        )
                )
            }
        }
    }
}

// Extension function for Text to get layout result
@Composable
fun Modifier.onTextLayout(
    onTextLayout: (TextLayoutResult) -> Unit
): Modifier = this.then(
    remember {
        object : DrawModifier {
            override fun ContentDrawScope.draw() {
                drawContent()
            }
        }
    }
)