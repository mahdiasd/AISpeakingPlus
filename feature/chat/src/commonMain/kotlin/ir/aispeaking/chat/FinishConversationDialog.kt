package ir.aispeaking.chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.chat.component.applePressEffect
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.GrammarErrorDetail
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_checked
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_spell_check
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameStars
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Finish Conversation & Evaluation Modal.
 *
 * Redesigned for maximum visual delight, glanceability, and interactive error inspection:
 * - Fluid Animated transition between Summary and Grammar Error Detail views.
 * - Interactive grammar stat card opening detailed corrections.
 * - Clean, punchy visual hierarchy eliminating long instructional text walls.
 * - Ergonomic Apple button hierarchy and corner close button.
 */
@Composable
fun FinishConversationDialog(
    visible: Boolean = true,
    evaluation: EvaluationSession,
    onContinueChatting: () -> Unit,
    onReplayStage: () -> Unit,
    onConfirmAndNext: () -> Unit,
    onDismissRequest: () -> Unit,
    onExitWithoutSave: () -> Unit = onDismissRequest
) {
    if (!visible) return

    var isViewingGrammarErrors by remember { mutableStateOf(false) }

    LaunchedEffect(visible, evaluation) {
        if (visible) {
            isViewingGrammarErrors = false
        }
    }

    GameModal(
        visible = visible,
        onDismiss = onDismissRequest
    ) {
        AnimatedContent(
            targetState = isViewingGrammarErrors,
            transitionSpec = {
                if (targetState) {
                    (slideInHorizontally(tween(260)) { -it } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(240)) { it } + fadeOut(tween(200)))
                } else {
                    (slideInHorizontally(tween(260)) { it } + fadeIn(tween(220)))
                        .togetherWith(slideOutHorizontally(tween(240)) { -it } + fadeOut(tween(200)))
                }
            },
            label = "FinishModalContentTransition",
            modifier = Modifier.fillMaxWidth()
        ) { viewingErrors ->
            if (viewingErrors) {
                GrammarErrorsDetailView(
                    errors = evaluation.grammarErrors,
                    onBack = { isViewingGrammarErrors = false }
                )
            } else {
                EvaluationSummaryView(
                    evaluation = evaluation,
                    onViewGrammarErrors = { isViewingGrammarErrors = true },
                    onContinueChatting = onContinueChatting,
                    onReplayStage = onReplayStage,
                    onConfirmAndNext = onConfirmAndNext,
                    onDismiss = onDismissRequest,
                    onExitWithoutSave = onExitWithoutSave
                )
            }
        }
    }
}

/**
 * Primary Glanceable Level Completion Summary View.
 */
@Composable
private fun EvaluationSummaryView(
    evaluation: EvaluationSession,
    onViewGrammarErrors: () -> Unit,
    onContinueChatting: () -> Unit,
    onReplayStage: () -> Unit,
    onConfirmAndNext: () -> Unit,
    onDismiss: () -> Unit,
    onExitWithoutSave: () -> Unit
) {
    val isDark = AppTheme.isDark
    val isGoalDone = evaluation.objectiveCompleted
    val stars = if (isGoalDone) evaluation.calculatedStars else 0
    val isPerfect = stars >= 3 && isGoalDone

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar: Close button & Score Chip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dismiss (X) button at top-right (Start anchor in RTL)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0x28FFFFFF) else Color(0x14000000))
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "بستن",
                    tint = if (isDark) Color(0xCCFFFFFF) else Color(0x99000000),
                    modifier = Modifier.size(15.dp)
                )
            }

            // Score Badge / Record Chip
            if (evaluation.score > 0) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AppleChatTheme.Gold.copy(alpha = 0.14f))
                        .border(1.dp, AppleChatTheme.Gold.copy(alpha = 0.45f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_star),
                        contentDescription = null,
                        tint = AppleChatTheme.Gold,
                        modifier = Modifier.size(13.dp)
                    )
                    GameText(
                        text = "+${evaluation.score.fa()} امتیاز",
                        size = 12.sp,
                        bold = true,
                        color = AppleChatTheme.Gold
                    )
                    if (evaluation.isHighScore) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AppleChatTheme.Coral.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            GameText(
                                text = "رکورد!",
                                size = 9.5.sp,
                                bold = true,
                                color = AppleChatTheme.Coral
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        // Hero Section: Celebratory Stars or Incomplete Target
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isGoalDone) {
                GameStars(
                    stars = stars,
                    size = 44.dp,
                    spacing = 8.dp,
                    animate = true,
                    earnedTint = AppleChatTheme.Gold
                )
                GameText(
                    text = when (stars) {
                        3 -> "شاهکار! همه ستاره‌ها رو گرفتی 🌟"
                        2 -> "آفرین! خیلی خوب صحبت کردی 👏"
                        1 -> "مرحله با موفقیت کامل شد ✅"
                        else -> "مرحله با موفقیت کامل شد! 🎯"
                    },
                    size = 17.sp,
                    bold = true,
                    align = TextAlign.Center
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AppleChatTheme.Coral.copy(alpha = 0.14f))
                        .border(1.5.dp, AppleChatTheme.Coral.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_alert_circle),
                        contentDescription = null,
                        tint = AppleChatTheme.Coral,
                        modifier = Modifier.size(28.dp)
                    )
                }
                GameText(
                    text = "هدف مرحله هنوز کامل نشده!",
                    size = 17.sp,
                    bold = true,
                    color = AppleChatTheme.Coral,
                    align = TextAlign.Center
                )
                GameText(
                    text = "برای دریافت ستاره، گفتگو را تا برآورده شدن هدف ادامه بده.",
                    size = 12.sp,
                    lineHeight = 18.sp,
                    color = Game.TextSecondary,
                    align = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Key Metrics: Grammar Errors & Hints Used
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Grammar Errors Card (Clickable when errors exist)
            val hasErrors = evaluation.grammarErrorsCount > 0
            val grammarCardShape = RoundedCornerShape(18.dp)
            val grammarColor = if (hasErrors) AppleChatTheme.Coral else AppleChatTheme.SuccessCyan
            val grammarBg = if (hasErrors) AppleChatTheme.Coral.copy(alpha = 0.12f) else AppleChatTheme.SuccessCyan.copy(alpha = 0.10f)
            val grammarBorder = if (hasErrors) AppleChatTheme.Coral.copy(alpha = 0.45f) else AppleChatTheme.SuccessCyan.copy(alpha = 0.35f)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(grammarCardShape)
                    .background(grammarBg)
                    .border(1.dp, grammarBorder, grammarCardShape)
                    .then(
                        if (hasErrors) {
                            Modifier.applePressEffect(targetScale = 0.95f, onClick = onViewGrammarErrors)
                        } else Modifier
                    )
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_spell_check),
                        contentDescription = null,
                        tint = grammarColor,
                        modifier = Modifier.size(16.dp)
                    )
                    GameText(
                        text = "خطای گرامری",
                        size = 12.sp,
                        color = Game.TextSecondary
                    )
                }

                GameText(
                    text = evaluation.grammarErrorsCount.fa(),
                    size = 22.sp,
                    bold = true,
                    color = grammarColor
                )

                if (hasErrors) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(grammarColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        GameText(
                            text = "مشاهده خطاها",
                            size = 10.5.sp,
                            bold = true,
                            color = grammarColor
                        )
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_left),
                            contentDescription = null,
                            tint = grammarColor,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                } else {
                    GameText(
                        text = "عالی و بدون خطا ✨",
                        size = 11.sp,
                        bold = true,
                        color = grammarColor
                    )
                }
            }

            // Hints Card
            val hintsCardShape = RoundedCornerShape(18.dp)
            val hintsUsed = evaluation.hintsUsedCount
            val hintsColor = if (hintsUsed == 0) AppleChatTheme.SuccessCyan else AppleChatTheme.Gold

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(hintsCardShape)
                    .background(hintsColor.copy(alpha = 0.10f))
                    .border(1.dp, hintsColor.copy(alpha = 0.35f), hintsCardShape)
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_lamp),
                        contentDescription = null,
                        tint = hintsColor,
                        modifier = Modifier.size(16.dp)
                    )
                    GameText(
                        text = "راهنما استفاده‌شده",
                        size = 12.sp,
                        color = Game.TextSecondary
                    )
                }

                GameText(
                    text = hintsUsed.fa(),
                    size = 22.sp,
                    bold = true,
                    color = hintsColor
                )

                GameText(
                    text = if (hintsUsed == 0) "بدون راهنما 💡" else "${hintsUsed.fa()} بار",
                    size = 11.sp,
                    bold = true,
                    color = hintsColor
                )
            }
        }

        // Coach Feedback Card
        if (isGoalDone && evaluation.feedbackFa.isNotBlank()) {
            val feedbackShape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(feedbackShape)
                    .background(AppleChatTheme.GlassPanelRaised)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, feedbackShape)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_lamp),
                    contentDescription = null,
                    tint = AppleChatTheme.SuccessCyan,
                    modifier = Modifier.size(16.dp).padding(top = 2.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    GameText(
                        text = "نظر مربی هوشمند",
                        size = 11.sp,
                        bold = true,
                        color = AppleChatTheme.SuccessCyan
                    )
                    GameText(
                        text = evaluation.feedbackFa.trim(),
                        size = 12.sp,
                        lineHeight = 18.sp,
                        color = Game.TextPrimary,
                        maxLines = 4
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Action Buttons: Clear Ergonomic Hierarchy
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isGoalDone) {
                GameButton(
                    text = "ثبت نتیجه و ادامه مسیر",
                    onClick = onConfirmAndNext,
                    modifier = Modifier.fillMaxWidth(),
                    style = GameButtonStyle.Primary,
                    height = 46.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isPerfect) {
                        GameButton(
                            text = "تلاش مجدد",
                            onClick = onReplayStage,
                            modifier = Modifier.weight(1f),
                            style = GameButtonStyle.Gold,
                            height = 40.dp,
                            textSize = 12.5.sp
                        )
                    }
                    GameButton(
                        text = "ادامه گفتگو",
                        onClick = onContinueChatting,
                        modifier = Modifier.weight(1f),
                        style = GameButtonStyle.Glass,
                        height = 40.dp,
                        textSize = 12.5.sp
                    )
                }
            } else {
                GameButton(
                    text = "ادامه گفتگو برای تکمیل هدف",
                    onClick = onContinueChatting,
                    modifier = Modifier.fillMaxWidth(),
                    style = GameButtonStyle.Primary,
                    height = 46.dp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GameButton(
                        text = "تلاش از ابتدا",
                        onClick = onReplayStage,
                        modifier = Modifier.weight(1f),
                        style = GameButtonStyle.Gold,
                        height = 40.dp,
                        textSize = 12.5.sp
                    )
                    GameButton(
                        text = "خروج بدون ثبت",
                        onClick = onExitWithoutSave,
                        modifier = Modifier.weight(1f),
                        style = GameButtonStyle.Glass,
                        height = 40.dp,
                        textSize = 12.5.sp
                    )
                }
            }
        }
    }
}

/**
 * Detailed Grammar Errors Inspection Subview.
 */
@Composable
private fun GrammarErrorsDetailView(
    errors: List<GrammarErrorDetail>,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
    ) {
        // Navigation Header (RTL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Start Anchor (RTL: Top Right) - Standard AppBackButton
            AppBackButton(onClick = onBack)

            // Centered Title
            GameText(
                text = "بررسی خطاهای گرامری (${errors.size.fa()})",
                size = 15.sp,
                bold = true,
                align = TextAlign.Center
            )

            // Spacer for symmetrical balance
            Spacer(modifier = Modifier.size(38.dp))
        }

        // Scrollable List of Errors
        if (errors.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AppleChatTheme.SuccessCyan.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_checked),
                        contentDescription = null,
                        tint = AppleChatTheme.SuccessCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
                GameText(
                    text = "هیچ خطای گرامری ثبت نشده است!",
                    size = 14.sp,
                    bold = true,
                    align = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                errors.forEachIndexed { index, error ->
                    GrammarErrorCard(
                        index = index + 1,
                        error = error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Return button
        GameButton(
            text = "متوجه شدم، بازگشت به کارنامه",
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            style = GameButtonStyle.Primary,
            height = 44.dp
        )
    }
}

/**
 * Individual Grammar Error Card with before/after comparison.
 */
@Composable
private fun GrammarErrorCard(
    index: Int,
    error: GrammarErrorDetail,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AppleChatTheme.GlassPanelRaised)
            .border(1.dp, AppleChatTheme.SpecularStrokeBrush, shape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Card Header: Item badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(AppleChatTheme.Coral.copy(alpha = 0.16f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                GameText(
                    text = "خطای شماره ${index.fa()}",
                    size = 11.sp,
                    bold = true,
                    color = AppleChatTheme.Coral
                )
            }
        }

        // Original (What user said - with Coral tint)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppleChatTheme.Coral.copy(alpha = 0.08f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            GameText(
                text = "جمله شما (دارای اشکال):",
                size = 10.5.sp,
                color = AppleChatTheme.Coral
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = error.original,
                    size = 13.sp,
                    bold = true,
                    color = Game.TextPrimary
                )
            }
        }

        // Corrected (How to say it properly - with SuccessCyan tint)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AppleChatTheme.SuccessCyan.copy(alpha = 0.10f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            GameText(
                text = "شکل صحیح:",
                size = 10.5.sp,
                color = AppleChatTheme.SuccessCyan
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = error.correction,
                    size = 13.sp,
                    bold = true,
                    color = AppleChatTheme.SuccessCyan
                )
            }
        }

        // Explanation (Persian)
        if (error.explanationFa.isNotBlank()) {
            GameText(
                text = error.explanationFa,
                size = 11.5.sp,
                lineHeight = 17.sp,
                color = Game.TextSecondary,
                modifier = Modifier.padding(horizontal = 2.dp)
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun FinishConversationDialogPreview() {
    AppTheme {
        FinishConversationDialog(
            evaluation = EvaluationSession(
                stageId = "stage_1",
                hintsUsedCount = 0,
                grammarErrorsCount = 2,
                objectiveCompleted = true,
                grammarErrors = listOf(
                    GrammarErrorDetail(
                        original = "I goes to school yesterday.",
                        correction = "I went to school yesterday.",
                        explanationFa = "برای زمان گذشته ساده از شکل گذشته فعل (went) استفاده می‌شود."
                    ),
                    GrammarErrorDetail(
                        original = "She don't like coffee.",
                        correction = "She doesn't like coffee.",
                        explanationFa = "برای فاعل سوم شخص مفرد (She) از doesn't استفاده می‌شود."
                    )
                ),
                calculatedStars = 2,
                score = 85,
                isHighScore = true,
                unlockedNextStage = true,
                feedbackFa = "عالی بود! مکالمه بسیار طبیعی و روان بود."
            ),
            onContinueChatting = {},
            onReplayStage = {},
            onConfirmAndNext = {},
            onDismissRequest = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun FinishConversationDialogLessStarsPreview() {
    AppTheme {
        FinishConversationDialog(
            evaluation = EvaluationSession(
                stageId = "stage_1",
                hintsUsedCount = 1,
                grammarErrorsCount = 0,
                objectiveCompleted = false,
                grammarErrors = emptyList(),
                calculatedStars = 0,
                score = 0,
                isHighScore = false,
                unlockedNextStage = false,
                feedbackFa = ""
            ),
            onContinueChatting = {},
            onReplayStage = {},
            onConfirmAndNext = {},
            onDismissRequest = {}
        )
    }
}
