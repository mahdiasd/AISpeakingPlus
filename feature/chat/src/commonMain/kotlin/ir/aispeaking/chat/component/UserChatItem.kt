package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.chat.Chat
import ir.aispeaking.domain.model.chat.ChatStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass User Chat Message Item.
 *
 * Characteristics:
 * - Rich Royal Azure & Electric Indigo glass gradient with specular top rim.
 * - Hero English copy with high legibility.
 * - Status Badge anchored at bottom-right (straddling bubble edge 50% in, 50% out):
 *   - Sleek micro spinner with breathing specular stroke while evaluating grammar.
 *   - Serene Cyan liquid glass pill for correct grammar (strictly NO green).
 *   - Champagne Gold liquid glass pill for grammar coaching tips.
 * - Playful Curved Dash Line smoothly connecting the badge to the GrammarTipCard below.
 * - Apple Coral retry chip for network failures.
 */
@Composable
fun UserChatItem(
    modifier: Modifier = Modifier,
    chat: Chat.User,
    onRetry: () -> Unit = {}
) {
    val status = chat.status
    val showBubbleBadge = when (status) {
        is ChatStatus.Sending -> true
        is ChatStatus.Answered -> true
        is ChatStatus.Failed -> false
    }

    val badgeBottomPadding by animateDpAsState(
        targetValue = if (showBubbleBadge) 12.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f),
        label = "user_bubble_badge_bottom_padding"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            val shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 6.dp,
                bottomEnd = 20.dp,
                bottomStart = 20.dp
            )

            // Bubble container with clearance for the protruding bottom-right badge
            Box(
                modifier = Modifier.padding(bottom = badgeBottomPadding)
            ) {
                // Rich Royal Azure & Indigo Glass Bubble
                Column(
                    modifier = Modifier
                        .widthIn(min = 96.dp, max = 310.dp)
                        .clip(shape)
                        .background(AppleChatTheme.UserBubbleBrush)
                        .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
                        .padding(horizontal = 15.dp, vertical = 11.dp)
                ) {
                    GameText(
                        text = chat.message,
                        size = 16.sp,
                        lineHeight = 24.sp,
                        latin = true,
                        color = Color.White
                    )
                }

                // Apple liquid glass grammar status badge anchored to bottom-right corner
                // (50% vertically inside the bubble, 50% outside over the background)
                androidx.compose.animation.AnimatedVisibility(
                    visible = showBubbleBadge,
                    enter = fadeIn(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) +
                            scaleIn(initialScale = 0.82f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)),
                    exit = fadeOut(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) +
                           scaleOut(targetScale = 0.82f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 10.dp)
                        .offset(y = 12.dp)
                ) {
                    UserGrammarBadge(status = status)
                }
            }

            // If grammar error, connect bubble badge to GrammarTipCard with CurvedDashLine
            if (status is ChatStatus.Answered && status.hasGrammarError) {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        GrammarTipCard(
                            feedback = status.grammar,
                            correctedSentence = status.correctedSentence,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                    CurvedDashLine(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(width = 24.dp, height = 32.dp),
                        color = if (AppTheme.isDark) AppleChatTheme.Gold else AppleChatTheme.GoldDeep
                    )
                }
            }

            // Retry HUD for failed messages
            if (status is ChatStatus.Failed) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    GameChip(
                        modifier = Modifier.padding(top = 6.dp),
                        text = "ارسال نشد؛ دوباره تلاش کن",
                        icon = Res.drawable.ic_refresh,
                        accent = AppleChatTheme.Coral,
                        border = AppleChatTheme.Coral.copy(alpha = 0.5f),
                        active = true,
                        onClick = onRetry
                    )
                }
            }
        }
    }
}

/**
 * Curved dashed line connecting the user bubble status badge to the grammar explanation card.
 */
@Composable
fun CurvedDashLine(
    modifier: Modifier = Modifier,
    color: Color = AppleChatTheme.Gold,
    strokeWidth: Dp = 2.dp,
    dashLength: Dp = 5.dp,
    gapLength: Dp = 4.dp
) {
    val density = LocalDensity.current
    Canvas(modifier = modifier) {
        val strokePx = with(density) { strokeWidth.toPx() }
        val dashPx = with(density) { dashLength.toPx() }
        val gapPx = with(density) { gapLength.toPx() }

        val w = size.width
        val h = size.height

        val path = Path().apply {
            // Start from top-right (right below the badge)
            moveTo(w - strokePx / 2f, 0f)
            // Cubic bezier down and curving left into the card's top edge
            cubicTo(
                x1 = w - strokePx / 2f, y1 = h * 0.55f,
                x2 = w * 0.45f, y2 = h - strokePx / 2f,
                x3 = 0f, y3 = h - strokePx / 2f
            )
        }

        val pathEffect = PathEffect.dashPathEffect(
            intervals = floatArrayOf(dashPx, gapPx),
            phase = 0f
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokePx,
                pathEffect = pathEffect,
                cap = StrokeCap.Round
            )
        )
    }
}

/**
 * Apple Liquid Glass Grammar Badge that lives on the bubble boundary.
 * Switches between loading evaluation, serene cyan success checkmark, and champagne gold tip badge.
 */
@Composable
private fun UserGrammarBadge(
    status: ChatStatus,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AnimatedContent(
            targetState = status,
            transitionSpec = {
                (fadeIn(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) +
                 scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)))
                    .togetherWith(
                        fadeOut(animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f)) +
                        scaleOut(targetScale = 0.85f, animationSpec = spring(dampingRatio = 0.82f, stiffness = 380f))
                    )
            },
            modifier = modifier
        ) { targetStatus ->
            when (targetStatus) {
                is ChatStatus.Sending -> {
                    GrammarLoadingBadge(isDark = isDark)
                }

                is ChatStatus.Answered -> {
                    if (!targetStatus.hasGrammarError) {
                        GrammarSuccessBadge(isDark = isDark)
                    } else {
                        GrammarErrorBadge(isDark = isDark)
                    }
                }

                else -> Unit
            }
        }
    }
}

/**
 * Loading badge: Apple micro spinner + "بررسی گرامر..." with iridescent breathing pulse.
 */
@Composable
private fun GrammarLoadingBadge(isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "grammar_loading_anim")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinner_rotation"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val shape = RoundedCornerShape(percent = 50)
    val backgroundColor = if (isDark) Color(0xF2151928) else Color(0xF8FFFFFF)
    val borderBrush = Brush.verticalGradient(
        listOf(
            AppleChatTheme.PrimaryAzure.copy(alpha = pulseAlpha * 0.7f),
            AppleChatTheme.PrimaryIndigo.copy(alpha = pulseAlpha * 0.35f)
        )
    )

    Row(
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = AppleChatTheme.PrimaryAzure.copy(alpha = 0.25f),
                clip = false
            )
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderBrush, shape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Canvas(modifier = Modifier.size(11.dp)) {
            val strokeWidth = 1.75.dp.toPx()
            // Ambient track
            drawCircle(
                color = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000),
                radius = (size.minDimension - strokeWidth) / 2f,
                style = Stroke(width = strokeWidth)
            )
            // Rotating active arc
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(
                        AppleChatTheme.PrimaryAzure.copy(alpha = 0.15f),
                        AppleChatTheme.PrimaryAzure,
                        AppleChatTheme.PrimaryIndigo
                    )
                ),
                startAngle = rotation,
                sweepAngle = 260f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        GameText(
            text = "بررسی گرامر...",
            size = 10.5.sp,
            bold = false,
            color = if (isDark) Color(0xEEFFFFFF) else Color(0xDD0F172A)
        )
    }
}

/**
 * Grammar OK status badge in Serene Cyan (strictly NO green) with specular glass rim.
 */
@Composable
private fun GrammarSuccessBadge(isDark: Boolean) {
    val shape = RoundedCornerShape(percent = 50)
    val backgroundColor = if (isDark) Color(0xF20F1826) else Color(0xF8FFFFFF)
    val borderBrush = Brush.verticalGradient(
        listOf(
            AppleChatTheme.SuccessCyan.copy(alpha = if (isDark) 0.85f else 0.65f),
            AppleChatTheme.SuccessCyan.copy(alpha = if (isDark) 0.35f else 0.25f)
        )
    )

    Row(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = AppleChatTheme.SuccessCyan.copy(alpha = 0.35f),
                clip = false
            )
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderBrush, shape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_done),
            contentDescription = null,
            tint = AppleChatTheme.SuccessCyan,
            modifier = Modifier.size(13.dp)
        )
        GameText(
            text = "گرامر درست بود",
            size = 11.sp,
            bold = true,
            color = AppleChatTheme.SuccessCyan
        )
    }
}

/**
 * Grammar coaching tip badge in Champagne Gold with specular glass rim.
 */
@Composable
private fun GrammarErrorBadge(isDark: Boolean) {
    val shape = RoundedCornerShape(percent = 50)
    val accent = if (isDark) AppleChatTheme.Gold else AppleChatTheme.GoldDeep
    val backgroundColor = if (isDark) Color(0xF2241F1A) else Color(0xF8FFFBEB)
    val borderBrush = Brush.verticalGradient(
        listOf(
            accent.copy(alpha = if (isDark) 0.85f else 0.65f),
            accent.copy(alpha = if (isDark) 0.35f else 0.25f)
        )
    )

    Row(
        modifier = Modifier
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.22f),
                spotColor = accent.copy(alpha = 0.35f),
                clip = false
            )
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderBrush, shape)
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_spell_check),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(13.dp)
        )
        GameText(
            text = "نکته گرامری",
            size = 11.sp,
            bold = true,
            color = accent
        )
    }
}

/**
 * Champagne Gold Frosted Glass Grammar Tip Card.
 * Displays corrected English sentence in LTR alongside Persian feedback.
 */
@Composable
private fun GrammarTipCard(
    feedback: String?,
    correctedSentence: String?,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(16.dp)
    val accent = if (isDark) AppleChatTheme.Gold else AppleChatTheme.GoldDeep
    Column(
        modifier = modifier
            .widthIn(max = 280.dp)
            .clip(shape)
            .background(if (isDark) Color(0xD91E1C1A) else Color(0xFFFFFBEB))
            .border(1.dp, accent.copy(alpha = if (isDark) 0.5f else 0.45f), shape)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_spell_check),
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(15.dp)
            )
            GameText(
                text = "نکته گرامری",
                size = 12.sp,
                bold = true,
                color = accent
            )
        }
        if (!correctedSentence.isNullOrBlank()) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = correctedSentence,
                    size = 13.sp,
                    bold = true,
                    lineHeight = 20.sp,
                    color = Game.TextPrimary
                )
            }
        }
        if (!feedback.isNullOrBlank()) {
            GameText(
                text = feedback,
                size = 12.sp,
                lineHeight = 19.sp,
                color = if (correctedSentence.isNullOrBlank()) Game.TextPrimary else Game.TextSecondary,
                align = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )
        } else if (correctedSentence.isNullOrBlank()) {
            GameText(
                text = "جمله شما از نظر گرامری نیاز به اصلاح دارد.",
                size = 12.sp,
                lineHeight = 19.sp,
                color = Game.TextPrimary
            )
        }
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

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun UserChatItemSendingPreview() {
    AppTheme {
        UserChatItem(
            chat = Chat.User(
                uid = "2",
                message = "I want to have a coffee.",
                status = ChatStatus.Sending
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun UserChatItemGrammarPreview() {
    AppTheme {
        UserChatItem(
            chat = Chat.User(
                uid = "3",
                message = "I wants a coffee.",
                status = ChatStatus.Answered(
                    grammar = "بهتر است بگویی «I want a coffee» چون فاعل I است.",
                    correctedSentence = "I want a coffee.",
                    hasGrammarError = true
                )
            )
        )
    }
}
