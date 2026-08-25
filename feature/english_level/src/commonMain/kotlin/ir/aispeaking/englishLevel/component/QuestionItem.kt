package ir.aispeaking.englishLevel.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.aispeaking.englishLevel.model.Question
import ir.aispeaking.englishLevel.utils.QuestionUtils
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun QuestionItem(
    modifier: Modifier,
    item: Question,
    selectedAnswerIndex: Int,
    onAnswer: (Int) -> Unit,
    showAnswers: Boolean
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        BodyMediumText(
            text = item.question,
            textDirection = TextDirection.Ltr,
            color = AppTheme.colors.onSurface
        )

        item.options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateClickable {
                        if (!showAnswers) {
                            onAnswer(index)
                        }
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    0.dp,
                    alignment = Alignment.Start
                )
            ) {
                RadioButton(
                    selected = index == selectedAnswerIndex || (showAnswers && index == item.answer),
                    onClick = { if (!showAnswers) onAnswer(index) },
                    colors = RadioButtonDefaults.colors().copy(
                        selectedColor = when {
                            showAnswers && index == item.answer -> AppTheme.colors.success
                            index == selectedAnswerIndex -> AppTheme.colors.primary
                            else -> AppTheme.colors.onSurface
                        },
                        unselectedColor = AppTheme.colors.onSurface
                    )
                )

                BodyMediumText(
                    text = option,
                    color = when {
                        showAnswers && index == item.answer -> AppTheme.colors.success
                        index == selectedAnswerIndex -> AppTheme.colors.primary
                        else -> AppTheme.colors.onSurface
                    }
                )


                if (index < item.options.size - 1) {
                    VerticalSpace(8.dp)
                }
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    val item = QuestionUtils.getAllQuestions().first()
    AppTheme {
        QuestionItem(
            modifier = Modifier
                .fillMaxWidth(),
            item = item,
            selectedAnswerIndex = -1,
            showAnswers = true,
            onAnswer = {

            }
        )
    }
}