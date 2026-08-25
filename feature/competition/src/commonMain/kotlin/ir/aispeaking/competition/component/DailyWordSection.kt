package ir.aispeaking.competition.component


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.competition.CompetitionUiEvent
import ir.aispeaking.competition.OnAction
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.domain.model.word.DailyWordProgress
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.btn_word_answer_accept
import ir.aispeaking.sharedui.ic_lightener
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.login_signup
import ir.aispeaking.sharedui.ui.core.button.AppButton
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.PointsColor
import ir.aispeaking.utils.MyUtils

private data class ItemColor(
    val containerColor: Color,
    val borderColor: Color,
    val textColor: Color,
)

@Composable
fun DailyWordSection(
    dailyWord: DailyWord,
    acceptableBtnLoading: Boolean = false,
    userSelectedWordIndex: Int? = null,
    onAction: OnAction,
    isLogin: Boolean,
) {
    val showAcceptBtn: Boolean by remember(userSelectedWordIndex, dailyWord) {
        mutableStateOf(dailyWord.wordProgress == null && userSelectedWordIndex != null)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape = AppTheme.shapes.roundMedium)
            .coloredShadow(
                color = AppTheme.colors.primary,
                borderRadius = 10.dp,
                shadowRadius = 10.dp,
                offsetY = 2.dp,
                alpha = 0.6f
            )
            .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
            .padding(8.dp)
            .padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            DualContentRow(
                rightContent = {
                    BodyMediumBoldText(text = "${dailyWord.points}")
                },
                leftContent = {
                    AppIcon(
                        icon = Res.drawable.ic_points,
                        size = 16.dp,
                        tint = PointsColor
                    )
                }
            )

            if (dailyWord.wordProgress != null) {
                AppIcon(
                    icon = Res.drawable.ic_lightener,
                    onClick = { onAction(CompetitionUiEvent.OnShowTranslateDialog(true)) })
            }
        }

        TitleBoldText(text = dailyWord.word)

        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .padding(horizontal = 12.dp),
            maxItemsInEachRow = 2,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            dailyWord.options.forEachIndexed { index, option ->
                val itemColor by getItemColor(
                    itemIndex = index,
                    dailyWord = dailyWord,
                    userSelectedWordIndex = userSelectedWordIndex
                )
                key(option) {
                    BodyMediumBoldText(
                        modifier = Modifier
                            .animateClickable {
                                if (dailyWord.wordProgress == null) {
                                    onAction(CompetitionUiEvent.OnWordSelectedAnswer(index))
                                }
                            }
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                color = itemColor.containerColor,
                                shape = AppTheme.shapes.roundSmall
                            )
                            .border(
                                1.dp,
                                color = itemColor.borderColor,
                                shape = AppTheme.shapes.roundSmall
                            )
                            .padding(8.dp),
                        text = option,
                        color = itemColor.textColor,
                        textStyle = AppTheme.typography.bodyMedium.copy(fontFamily = AppTheme.typography.persianRegular),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            visible = showAcceptBtn
        ) {
            AppButton(
                modifier = Modifier.wrapContentWidth(),
                isLoading = acceptableBtnLoading,
                text = when (isLogin) {
                    true -> Res.string.btn_word_answer_accept
                    false -> Res.string.login_signup
                }
            ) {
                when (isLogin) {
                    true -> onAction(CompetitionUiEvent.OnSendAnswer)
                    false -> onAction(CompetitionUiEvent.OnNavigateToLogin)
                }

            }
        }
    }

}

@Composable
private fun getItemColor(
    itemIndex: Int,
    dailyWord: DailyWord,
    userSelectedWordIndex: Int?,
): State<ItemColor> {
    val colors = AppTheme.colors

    return remember(itemIndex, dailyWord, userSelectedWordIndex) {
        derivedStateOf {
            when {
                userSelectedWordIndex == null && dailyWord.wordProgress == null -> {
                    ItemColor(
                        containerColor = Color.Transparent,
                        borderColor = colors.outline,
                        textColor = colors.onSurface
                    )
                }

                (itemIndex == userSelectedWordIndex || itemIndex == dailyWord.wordProgress?.selectedOptionIndex)
                        && dailyWord.wordProgress?.isCorrect == true -> {
                    ItemColor(
                        containerColor = colors.success.copy(alpha = 0.5f),
                        borderColor = colors.success,
                        textColor = colors.onSuccess
                    )
                }

                (itemIndex == userSelectedWordIndex || itemIndex == dailyWord.wordProgress?.selectedOptionIndex)
                        && dailyWord.wordProgress?.isCorrect == false -> {
                    ItemColor(
                        containerColor = colors.error.copy(alpha = 0.5f),
                        borderColor = colors.error,
                        textColor = colors.onError
                    )
                }

                itemIndex == userSelectedWordIndex && dailyWord.wordProgress == null -> {
                    ItemColor(
                        containerColor = colors.primaryContainer.copy(alpha = 0.6f),
                        borderColor = colors.primary,
                        textColor = colors.onPrimaryContainer
                    )
                }

                itemIndex == dailyWord.answerIndex && dailyWord.wordProgress != null -> {
                    ItemColor(
                        containerColor = colors.success.copy(alpha = 0.6f),
                        borderColor = colors.success,
                        textColor = colors.onSuccess
                    )
                }

                else -> {
                    ItemColor(
                        containerColor = Color.Transparent,
                        borderColor = colors.outline,
                        textColor = colors.onSurface
                    )
                }
            }
        }
    }
}


/**
↓ ↓ ------------ Previews ----------------------- ↓ ↓
 */
@LightDarkPreview
@Composable
fun PreviewInitialState() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.surface)
                .padding(16.dp)
        ) {
            TitleBoldText(text = "Initial State")
            DailyWordSection(
                dailyWord = FakeData.provideDailyWord(),
                acceptableBtnLoading = false,
                userSelectedWordIndex = null, // wordProgress is null
                onAction = {},
                isLogin = false
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PreviewSelectedWithoutProgress() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.surface)
                .padding(16.dp)
        ) {
            TitleBoldText(text = "Selected Without Progress")
            DailyWordSection(
                dailyWord = FakeData.provideDailyWord(),
                acceptableBtnLoading = false,
                userSelectedWordIndex = 2, // still no progress
                onAction = {},
                isLogin = false // simulate selection at index 2
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PreviewCorrectAnswer() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.surface)
                .padding(16.dp)
        ) {
            TitleBoldText(text = "Correct Answer")
            DailyWordSection(
                dailyWord = FakeData.provideDailyWord().copy(
                    wordProgress = DailyWordProgress(
                        uid = MyUtils.generateStringUUID(),
                        selectedOptionIndex = 0, // user selected the correct option
                        score = 10,
                        isCorrect = true
                    )
                ),
                acceptableBtnLoading = false,
                userSelectedWordIndex = 0,
                onAction = {},
                isLogin = false
            )
        }
    }
}

@LightDarkPreview
@Composable
fun PreviewIncorrectAnswer() {
    AppTheme {
        Column(
            modifier = Modifier
                .background(AppTheme.colors.surface)
                .padding(16.dp)
        ) {
            TitleBoldText(text = "Incorrect Answer")
            DailyWordSection(
                dailyWord = FakeData.provideDailyWord().copy(
                    wordProgress = DailyWordProgress(
                        uid = MyUtils.generateStringUUID(),
                        selectedOptionIndex = 2, // user selected a wrong option
                        score = 0,
                        isCorrect = false
                    )
                ),
                acceptableBtnLoading = false,
                userSelectedWordIndex = 2,
                onAction = {},
                isLogin = false
            )
        }
    }
}
