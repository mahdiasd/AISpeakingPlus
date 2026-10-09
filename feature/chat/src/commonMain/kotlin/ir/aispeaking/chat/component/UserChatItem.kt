package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.core.text.SelectableText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.coroutines.delay

@Composable
fun UserChatItem(
    modifier: Modifier = Modifier,
    chat: Chat.User,
    onRetry: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (textRef) = createRefs()
        SelectableText(
            modifier = Modifier
                .widthIn(min = 170.dp, max = 280.dp)
                .constrainAs(textRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                }
                .background(AppTheme.colors.userChatContainer, shape = AppTheme.shapes.roundMedium)
                .padding(4.dp)
                .padding(bottom = 6.dp),
            text = chat.message,
            onSelected = { }
        )
        val status = chat.status
        when (status) {
            is ChatStatus.Answered -> {
                val grammarIsOk = status.grammar.isEmpty()
                when (grammarIsOk) {
                    true -> {
                        OkGrammarContent(
                            modifier = Modifier.constrainAs(createRef()) {
                                top.linkTo(textRef.bottom)
                                bottom.linkTo(textRef.bottom)
                                end.linkTo(textRef.end, 8.dp)
                            }
                        )
                    }

                    false -> {
                        WrongGrammarContent(
                            modifier = Modifier,
                            alignTopRef = textRef,
                            grammarHintText = status.grammar
                        )
                    }
                }
            }

            is ChatStatus.Failed -> {
                DualContentRow(
                    modifier = Modifier
                        .animateClickable { onRetry() }
                        .constrainAs(createRef()) {
                            top.linkTo(textRef.bottom)
                            bottom.linkTo(textRef.bottom)
                            end.linkTo(textRef.end, 8.dp)
                        }
                        .background(
                            color = AppTheme.colors.userChatContainer,
                            shape = AppTheme.shapes.roundMedium
                        )
                        .border(
                            width = 0.25.dp,
                            color = AppTheme.colors.onSurface,
                            shape = AppTheme.shapes.roundMedium
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        space = 4.dp,
                        alignment = Alignment.End
                    ),
                    leftContent = {
                        LabelMediumBoldText(
                            modifier = Modifier.animateClickable { onRetry() },
                            text = "تلاش مجدد",
                            color = AppTheme.colors.error
                        )
                    },
                    rightContent = {
                        AppIcon(
                            modifier = Modifier
                                .background(AppTheme.colors.error, shape = CircleShape)
                                .padding(2.dp),
                            size = 16.dp,
                            icon = Res.drawable.ic_refresh,
                            tint = AppTheme.colors.onError,
                            onClick = onRetry
                        )
                    },
                )
            }

            is ChatStatus.Sending -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .constrainAs(createRef()) {
                            top.linkTo(textRef.bottom)
                            bottom.linkTo(textRef.bottom)
                            end.linkTo(textRef.end, 8.dp)
                        }
                        .size(24.dp)
                        .background(AppTheme.colors.userChatContainer, CircleShape)
                        .padding(4.dp),
                    strokeWidth = 1.dp,
                    color = AppTheme.colors.primary
                )
            }
        }
    }
}

@Composable
fun ConstraintLayoutScope.WrongGrammarContent(
    modifier: Modifier = Modifier,
    grammarHintText: String,
    alignTopRef: ConstrainedLayoutReference
) {
    var showLeftContent by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(2000)
        showLeftContent = false
    }

    Column(
        modifier = Modifier.constrainAs(createRef()) {
            top.linkTo(alignTopRef.bottom, (-13).dp)
            end.linkTo(alignTopRef.end, 4.dp)
        },
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(
            space = 0.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        DualContentRow(
            modifier = modifier
                .background(
                    color = AppTheme.colors.userChatContainer,
                    shape = AppTheme.shapes.roundMedium
                )
                .border(
                    width = 0.25.dp,
                    color = AppTheme.colors.onSurface,
                    shape = AppTheme.shapes.roundMedium
                )
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.End
            ),
            leftContent = {
                AnimatedVisibility(
                    visible = showLeftContent,
                    enter = expandHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        expandFrom = Alignment.End
                    ),
                    exit = shrinkHorizontally(
                        animationSpec = tween(300, easing = FastOutSlowInEasing),
                        shrinkTowards = Alignment.End
                    )
                ) {
                    LabelMediumBoldText(
                        text = "اشکال گرامری",
                        color = AppTheme.colors.error
                    )
                }
            },
            rightContent = {
                AppIcon(
                    modifier = Modifier
                        .background(AppTheme.colors.error, shape = CircleShape)
                        .padding(2.dp),
                    size = 16.dp,
                    icon = Res.drawable.ic_close,
                    tint = AppTheme.colors.onError
                )
            },
        )

        Row(
            modifier = Modifier.padding(end = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .background(
                        color = Color(0xF01E293B),
                        shape = AppTheme.shapes.roundedSemiLarge
                    )
                    .border(
                        width = 1.dp,
                        color = AppTheme.colors.primary,
                        shape = AppTheme.shapes.roundedSemiLarge
                    )
                    .widthIn(max = 260.dp)
                    .padding(10.dp)
            ) {
                LabelMediumText(
                    text = grammarHintText,
                    color = Color(0xFFF1F5F9),
                    persianFont = true
                )
            }
            CurvedDashLine(
                modifier = Modifier
                    .rotate(180f)
                    .size(width = 20.dp, height = 40.dp)
            )
        }
    }
}

@Composable
fun OkGrammarContent(modifier: Modifier = Modifier) {
    var showLeftContent by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(2000)
        showLeftContent = false
    }

    Row(
        modifier = modifier
            .background(
                color = AppTheme.colors.userChatContainer,
                shape = when (showLeftContent) {
                    true -> AppTheme.shapes.roundMedium
                    false -> CircleShape
                }
            )
            .border(
                0.25.dp,
                color = AppTheme.colors.onSurface,
                shape = when (showLeftContent) {
                    true -> AppTheme.shapes.roundMedium
                    false -> CircleShape
                }
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterHorizontally)
    ) {
        AnimatedVisibility(
            visible = showLeftContent,
            enter = expandHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                expandFrom = Alignment.Start
            ),
            exit = shrinkHorizontally(
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                shrinkTowards = Alignment.Start
            )
        ) {
            LabelSmallBoldText(
                text = "گرامر درسته",
                color = AppTheme.colors.success
            )
        }
        AppIcon(
            modifier = Modifier
                .background(AppTheme.colors.success, shape = CircleShape)
                .padding(2.dp),
            icon = Res.drawable.ic_done,
            size = 16.dp,
            tint = AppTheme.colors.onSuccess
        )
    }
}

@Composable
fun CurvedDashLine(
    modifier: Modifier = Modifier,
    color: Color = AppTheme.colors.primary,
    strokeWidth: Float = 5f,
    dashLength: Float = 10f,
    gapLength: Float = 25f
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val path = Path().apply {
            moveTo(0f, height)
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = height - 2 * width,
                    right = 2 * width,
                    bottom = height
                ),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
        }

        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashLength, gapLength),
            phase = 0f
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                pathEffect = pathEffect,
                cap = StrokeCap.Round
            )
        )
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun UserChatItemPreview() {
    AppTheme {
        UserChatItem(
            chat = Chat.User(
                uid = "1",
                message = "I want to have a coffee.",
                status = ChatStatus.Answered(grammar = "")
            )
        )
    }
}
