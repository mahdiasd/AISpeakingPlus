package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameStars
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa

/** "Level complete" result card: stars first, then what affected them, then what to do next. */
@Composable
fun FinishConversationDialog(
    visible: Boolean = true,
    evaluation: EvaluationSession,
    onContinueChatting: () -> Unit,
    onReplayStage: () -> Unit,
    onConfirmAndNext: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val stars = evaluation.calculatedStars
    val isPerfect = stars >= 3
    val accent = if (isPerfect) Game.Mint else if (stars > 0) Game.Gold else Game.Coral

    GameModal(visible = visible, onDismiss = onDismissRequest) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with soft glow in the accent color
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(accent.copy(alpha = 0.28f), Color.Transparent))
                    )
                    .padding(top = 28.dp, bottom = 8.dp, start = 20.dp, end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameStars(
                    stars = stars,
                    size = 54.dp,
                    spacing = 8.dp,
                    animate = true,
                    earnedTint = Game.Gold,
                    emptyTint = Color(0x44FFFFFF)
                )
                GameText(
                    text = when (stars) {
                        3 -> "عالی بود! کامل تمامش کردی"
                        2 -> "آفرین! خیلی خوب بود"
                        1 -> "خوب بود، ادامه بده"
                        else -> "این بار ستاره نگرفتی"
                    },
                    size = 20.sp,
                    bold = true,
                    align = TextAlign.Center
                )
                if (evaluation.score > 0) {
                    GameText(
                        text = "امتیاز تو: ${evaluation.score.fa()}",
                        size = 14.sp,
                        bold = true,
                        color = Game.Gold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // What changed the stars
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ResultStat(
                        label = "راهنما",
                        value = evaluation.hintsUsedCount.fa(),
                        good = evaluation.hintsUsedCount == 0,
                        modifier = Modifier.weight(1f)
                    )
                    ResultStat(
                        label = "خطای گرامری",
                        value = evaluation.grammarErrorsCount.fa(),
                        good = evaluation.grammarErrorsCount == 0,
                        modifier = Modifier.weight(1f)
                    )
                }

                GameText(
                    text = when (stars) {
                        3 -> "بدون خطا و بدون راهنما، هر ۳ ستاره را گرفتی."
                        2 -> "یک مورد کسر امتیاز (خطا یا راهنما) داشتی، پس ۲ ستاره گرفتی."
                        1 -> "دو مورد کسر امتیاز داشتی، پس ۱ ستاره گرفتی."
                        else -> "با ۳ خطا یا راهنما ستاره‌ای تعلق نمی‌گیرد. دوباره امتحان کن!"
                    },
                    size = 13.sp,
                    lineHeight = 21.sp,
                    color = Game.TextSecondary
                )

                if (evaluation.feedbackFa.isNotBlank()) {
                    val shape = RoundedCornerShape(16.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Game.PanelRaised, shape)
                            .border(1.dp, Game.Stroke, shape)
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GameText(text = "نظر مربی", size = 12.sp, bold = true, color = Game.Sky)
                        GameText(
                            text = evaluation.feedbackFa,
                            size = 13.sp,
                            lineHeight = 21.sp
                        )
                    }
                }
            }

        }

        // Actions: one obvious primary, the rest quieter
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameButton(
                text = "ثبت نتیجه و ادامه مسیر",
                onClick = onConfirmAndNext,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Primary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!isPerfect) {
                    GameButton(
                        text = "تلاش دوباره",
                        onClick = onReplayStage,
                        modifier = Modifier.weight(1f),
                        style = GameButtonStyle.Gold,
                        height = 46.dp,
                        textSize = 14.sp
                    )
                }
                GameButton(
                    text = "ادامه گفتگو",
                    onClick = onContinueChatting,
                    modifier = Modifier.weight(1f),
                    style = GameButtonStyle.Glass,
                    height = 46.dp,
                    textSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ResultStat(
    label: String,
    value: String,
    good: Boolean,
    modifier: Modifier = Modifier
) {
    val color = if (good) Game.Mint else Game.Coral
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), shape)
            .border(1.dp, color.copy(alpha = 0.4f), shape)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        GameText(text = value, size = 24.sp, bold = true, color = color)
        GameText(text = label, size = 12.sp, color = Game.TextSecondary)
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun FinishConversationDialogPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        FinishConversationDialog(
            evaluation = EvaluationSession(
                stageId = "stage_1",
                hintsUsedCount = 0,
                grammarErrorsCount = 0,
                objectiveCompleted = true,
                grammarErrors = emptyList(),
                calculatedStars = 3,
                score = 100,
                isHighScore = true,
                unlockedNextStage = true,
                feedbackFa = "عالی بود! مکالمه بسیار طبیعی و بدون اشکال بود."
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
    ir.aispeaking.sharedui.ui.them.AppTheme {
        FinishConversationDialog(
            evaluation = EvaluationSession(
                stageId = "stage_1",
                hintsUsedCount = 1,
                grammarErrorsCount = 1,
                objectiveCompleted = true,
                grammarErrors = emptyList(),
                calculatedStars = 1,
                score = 65,
                isHighScore = false,
                unlockedNextStage = true,
                feedbackFa = "خوب بود! با تکرار بدون خطا می‌توانی ستاره‌های بیشتری بگیری."
            ),
            onContinueChatting = {},
            onReplayStage = {},
            onConfirmAndNext = {},
            onDismissRequest = {}
        )
    }
}
