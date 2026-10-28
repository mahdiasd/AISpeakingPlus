package ir.aispeaking.sharedui.ui.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlin.math.roundToInt

/**
 * Full-screen host for displaying minimal spotlight tours with curved dashed connectors.
 */
@Composable
fun AppGuideHost(
    state: GuideState,
    modifier: Modifier = Modifier
) {
    if (!state.isVisible) return

    val currentStep = state.currentStep ?: return
    val density = LocalDensity.current
    val isDark = AppTheme.isDark
    val accent = if (isDark) Game.Gold else Game.GoldDeep

    var hostBoundsInRoot by remember { mutableStateOf(Rect.Zero) }
    var hostSize by remember { mutableStateOf(IntSize.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }

    // Convert target coordinates from root to host-relative
    val rawTargetRect = state.currentTargetRect
    val relativeTargetRect = remember(rawTargetRect, hostBoundsInRoot) {
        if (rawTargetRect != null && hostBoundsInRoot != Rect.Zero) {
            Rect(
                left = rawTargetRect.left - hostBoundsInRoot.left,
                top = rawTargetRect.top - hostBoundsInRoot.top,
                right = rawTargetRect.right - hostBoundsInRoot.left,
                bottom = rawTargetRect.bottom - hostBoundsInRoot.top
            )
        } else {
            rawTargetRect
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                hostBoundsInRoot = coordinates.boundsInRoot()
                hostSize = coordinates.size
            }
            .pointerInput(state.currentStepIndex) {
                // Intercept all background touches safely
                detectTapGestures { /* consume */ }
            }
    ) {
        // Spotlight Canvas: Scrim + Cutout + Curved Dashed Line
        Canvas(modifier = Modifier.fillMaxSize()) {
            val padPx = 8.dp.toPx()
            val cornerRadiusPx = 14.dp.toPx()

            // 1. Draw scrim with cutout using EvenOdd fill
            val scrimPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                if (relativeTargetRect != null) {
                    val paddedRect = relativeTargetRect.inflate(padPx)
                    addRoundRect(RoundRect(paddedRect, CornerRadius(cornerRadiusPx, cornerRadiusPx)))
                }
            }

            drawPath(
                path = scrimPath,
                color = Color.Black.copy(alpha = 0.52f)
            )

            // 2. Glowing accent rim around target
            if (relativeTargetRect != null) {
                val paddedRect = relativeTargetRect.inflate(padPx)
                drawRoundRect(
                    color = accent.copy(alpha = 0.85f),
                    topLeft = Offset(paddedRect.left, paddedRect.top),
                    size = Size(paddedRect.width, paddedRect.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3. Curved dashed line connecting card to target
                if (cardSize.width > 0 && cardSize.height > 0) {
                    val isTargetInBottomHalf = relativeTargetRect.center.y > size.height * 0.5f
                    val placeAbove = when (currentStep.alignment) {
                        GuideAlignment.Top -> true
                        GuideAlignment.Bottom -> false
                        GuideAlignment.Auto -> isTargetInBottomHalf
                    }

                    val marginPx = 16.dp.toPx()
                    val targetCenterX = relativeTargetRect.center.x
                    val desiredCardLeft = targetCenterX - (cardSize.width / 2f)
                    val cardLeft = desiredCardLeft.coerceIn(marginPx, size.width - cardSize.width - marginPx)

                    val cardTop = if (placeAbove) {
                        (relativeTargetRect.top - padPx - cardSize.height - 32.dp.toPx())
                            .coerceAtLeast(marginPx)
                    } else {
                        (relativeTargetRect.bottom + padPx + 32.dp.toPx())
                            .coerceAtMost(size.height - cardSize.height - marginPx)
                    }

                    val lineStart: Offset
                    val lineEnd: Offset

                    if (placeAbove) {
                        lineStart = Offset(
                            x = (cardLeft + cardSize.width * 0.5f).coerceIn(cardLeft + 20f, cardLeft + cardSize.width - 20f),
                            y = cardTop + cardSize.height
                        )
                        lineEnd = Offset(
                            x = targetCenterX,
                            y = relativeTargetRect.top - padPx
                        )
                    } else {
                        lineStart = Offset(
                            x = (cardLeft + cardSize.width * 0.5f).coerceIn(cardLeft + 20f, cardLeft + cardSize.width - 20f),
                            y = cardTop
                        )
                        lineEnd = Offset(
                            x = targetCenterX,
                            y = relativeTargetRect.bottom + padPx
                        )
                    }

                    val dashPx = 5.dp.toPx()
                    val gapPx = 4.dp.toPx()
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)

                    val connectorPath = Path().apply {
                        moveTo(lineStart.x, lineStart.y)
                        val midY = (lineStart.y + lineEnd.y) / 2f
                        cubicTo(
                            x1 = lineStart.x, y1 = midY,
                            x2 = lineEnd.x, y2 = midY,
                            x3 = lineEnd.x, y3 = lineEnd.y
                        )
                    }

                    drawPath(
                        path = connectorPath,
                        color = accent,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = pathEffect,
                            cap = StrokeCap.Round
                        )
                    )
                }
            }
        }

        // Guide Card Placement
        val screenWidth = hostSize.width.toFloat()
        val screenHeight = hostSize.height.toFloat()

        if (screenWidth > 0 && screenHeight > 0) {
            val isTargetInBottomHalf = (relativeTargetRect?.center?.y ?: (screenHeight / 2f)) > screenHeight * 0.5f
            val placeAbove = when (currentStep.alignment) {
                GuideAlignment.Top -> true
                GuideAlignment.Bottom -> false
                GuideAlignment.Auto -> isTargetInBottomHalf
            }

            val marginPx = with(density) { 16.dp.toPx() }
            val targetCenterX = relativeTargetRect?.center?.x ?: (screenWidth / 2f)
            val desiredCardLeft = targetCenterX - (cardSize.width / 2f)
            val cardLeft = desiredCardLeft.coerceIn(marginPx, (screenWidth - cardSize.width - marginPx).coerceAtLeast(marginPx))

            val padPx = with(density) { 8.dp.toPx() }
            val cardTop = if (relativeTargetRect != null) {
                if (placeAbove) {
                    (relativeTargetRect.top - padPx - cardSize.height - with(density) { 32.dp.toPx() })
                        .coerceAtLeast(marginPx)
                } else {
                    (relativeTargetRect.bottom + padPx + with(density) { 32.dp.toPx() })
                        .coerceAtMost((screenHeight - cardSize.height - marginPx).coerceAtLeast(marginPx))
                }
            } else {
                (screenHeight / 2f) - (cardSize.height / 2f)
            }

            AnimatedVisibility(
                visible = state.isVisible,
                enter = fadeIn() + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut() + scaleOut(targetScale = 0.92f),
                modifier = Modifier.offset {
                    IntOffset(cardLeft.roundToInt(), cardTop.roundToInt())
                }
            ) {
                AppGuideCard(
                    step = currentStep,
                    stepIndex = state.currentStepIndex,
                    totalSteps = state.totalSteps,
                    onNext = { state.next() },
                    onPrevious = { state.previous() },
                    onSkip = { state.skip() },
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        cardSize = coordinates.size
                    }
                )
            }
        }
    }
}
