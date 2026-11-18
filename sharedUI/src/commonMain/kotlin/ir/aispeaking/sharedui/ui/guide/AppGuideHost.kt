package ir.aispeaking.sharedui.ui.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.TwoWayConverter
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val RectVectorConverter = TwoWayConverter<Rect, AnimationVector4D>(
    convertToVector = { AnimationVector4D(it.left, it.top, it.right, it.bottom) },
    convertFromVector = { Rect(it.v1, it.v2, it.v3, it.v4) }
)

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

    // Animated spotlight target bounds for smooth morphing transitions between steps
    val animRect = remember { Animatable(Rect.Zero, RectVectorConverter) }
    var hasInitializedTarget by remember { mutableStateOf(false) }

    LaunchedEffect(relativeTargetRect) {
        if (relativeTargetRect != null && relativeTargetRect.width > 0f) {
            if (!hasInitializedTarget) {
                animRect.snapTo(relativeTargetRect)
                hasInitializedTarget = true
            } else {
                animRect.animateTo(
                    targetValue = relativeTargetRect,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    val screenWidth = hostSize.width.toFloat()
    val screenHeight = hostSize.height.toFloat()

    val effectiveTargetRect = if (hasInitializedTarget) animRect.value else relativeTargetRect

    val isTargetInBottomHalf = (effectiveTargetRect?.center?.y ?: (screenHeight / 2f)) > screenHeight * 0.5f
    val placeAbove = when (currentStep.alignment) {
        GuideAlignment.Top -> true
        GuideAlignment.Bottom -> false
        GuideAlignment.Auto -> isTargetInBottomHalf
    }

    val marginPx = with(density) { 16.dp.toPx() }
    val targetCenterX = effectiveTargetRect?.center?.x ?: (screenWidth / 2f)
    val desiredCardLeft = targetCenterX - (cardSize.width / 2f)
    val cardLeft = if (screenWidth > 0) {
        desiredCardLeft.coerceIn(marginPx, (screenWidth - cardSize.width - marginPx).coerceAtLeast(marginPx))
    } else marginPx

    val padPx = with(density) { 8.dp.toPx() }
    val cardTop = if (screenHeight > 0 && effectiveTargetRect != null && effectiveTargetRect.width > 0f) {
        if (placeAbove) {
            (effectiveTargetRect.top - padPx - cardSize.height - with(density) { 32.dp.toPx() })
                .coerceAtLeast(marginPx)
        } else {
            (effectiveTargetRect.bottom + padPx + with(density) { 32.dp.toPx() })
                .coerceAtMost((screenHeight - cardSize.height - marginPx).coerceAtLeast(marginPx))
        }
    } else {
        if (screenHeight > 0) (screenHeight / 2f) - (cardSize.height / 2f) else marginPx
    }

    var hasInitializedCardPos by remember { mutableStateOf(false) }
    val animCardLeft = remember { Animatable(0f) }
    val animCardTop = remember { Animatable(0f) }

    LaunchedEffect(cardLeft, cardTop, cardSize) {
        if (cardSize.width > 0 && cardSize.height > 0) {
            if (!hasInitializedCardPos) {
                animCardLeft.snapTo(cardLeft)
                animCardTop.snapTo(cardTop)
                hasInitializedCardPos = true
            } else {
                launch {
                    animCardLeft.animateTo(
                        cardLeft,
                        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    )
                }
                launch {
                    animCardTop.animateTo(
                        cardTop,
                        spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)
                    )
                }
            }
        }
    }

    val curCardLeft = if (hasInitializedCardPos) animCardLeft.value else cardLeft
    val curCardTop = if (hasInitializedCardPos) animCardTop.value else cardTop

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
            val cornerRadiusPx = if (currentStep.isCircle) {
                ((effectiveTargetRect?.width ?: 0f) + padPx * 2f) / 2f
            } else {
                currentStep.cornerRadius?.toPx() ?: 14.dp.toPx()
            }

            // 1. Draw scrim with cutout using EvenOdd fill
            val scrimPath = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(Rect(0f, 0f, size.width, size.height))
                if (effectiveTargetRect != null && effectiveTargetRect.width > 0f) {
                    val paddedRect = effectiveTargetRect.inflate(padPx)
                    addRoundRect(RoundRect(paddedRect, CornerRadius(cornerRadiusPx, cornerRadiusPx)))
                }
            }

            drawPath(
                path = scrimPath,
                color = Color.Black.copy(alpha = 0.52f)
            )

            // 2. Glowing accent rim around target
            if (effectiveTargetRect != null && effectiveTargetRect.width > 0f) {
                val paddedRect = effectiveTargetRect.inflate(padPx)
                drawRoundRect(
                    color = accent.copy(alpha = 0.85f),
                    topLeft = Offset(paddedRect.left, paddedRect.top),
                    size = Size(paddedRect.width, paddedRect.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = 2.dp.toPx())
                )

                // 3. Curved dashed line connecting card to target
                if (cardSize.width > 0 && cardSize.height > 0) {
                    val lineStart: Offset
                    val lineEnd: Offset

                    val cardMinX = curCardLeft + 20.dp.toPx()
                    val cardMaxX = curCardLeft + cardSize.width - 20.dp.toPx()
                    val anchorX = if (cardMaxX >= cardMinX) targetCenterX.coerceIn(cardMinX, cardMaxX) else curCardLeft + cardSize.width / 2f

                    if (placeAbove) {
                        lineStart = Offset(
                            x = anchorX,
                            y = curCardTop + cardSize.height
                        )
                        lineEnd = Offset(
                            x = targetCenterX,
                            y = effectiveTargetRect.top - padPx
                        )
                    } else {
                        lineStart = Offset(
                            x = anchorX,
                            y = curCardTop
                        )
                        lineEnd = Offset(
                            x = targetCenterX,
                            y = effectiveTargetRect.bottom + padPx
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
        if (screenWidth > 0 && screenHeight > 0) {
            AnimatedVisibility(
                visible = state.isVisible,
                enter = fadeIn() + scaleIn(
                    initialScale = 0.92f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
                ),
                exit = fadeOut() + scaleOut(targetScale = 0.92f),
                modifier = Modifier.offset {
                    IntOffset(curCardLeft.roundToInt(), curCardTop.roundToInt())
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
