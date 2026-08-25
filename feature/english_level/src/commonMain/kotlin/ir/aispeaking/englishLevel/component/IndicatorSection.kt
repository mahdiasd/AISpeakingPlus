package ir.aispeaking.englishLevel.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.aispeaking.englishLevel.EnglishLevelUiEvent
import ir.aispeaking.englishLevel.OnAction
import ir.aispeaking.englishLevel.model.Question
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.english_level_screen_btn
import ir.aispeaking.sharedui.next
import ir.aispeaking.sharedui.previous
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@Composable
fun IndicatorSection(
    pagerState: PagerState,
    userAnswers: Map<Uuid, Int>,
    questions: ImmutableList<Question>,
    onAction: OnAction
) {
    val cScope = rememberCoroutineScope()
    val hasNext by remember(userAnswers, questions, pagerState.currentPage) {
        mutableStateOf(
            questions.getOrNull(pagerState.currentPage)?.let {
                userAnswers.getOrElse(it.id, { -1 }) != -1
            } ?: false
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colors.surfaceContainerLow)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        BodyMediumText(
            modifier = Modifier
                .animateClickable {
                    cScope.launch {
                        if (pagerState.currentPage > 0)
                            pagerState.animateScrollToPage(page = pagerState.currentPage - 1)
                    }
                },
            color = if (pagerState.currentPage != 0) AppTheme.colors.onPrimary else AppTheme.colors.outline,
            text = stringResource(Res.string.previous)
        )

        BodyMediumText(
            textDirection = TextDirection.Ltr,
            text = "${pagerState.currentPage + 1} / ${pagerState.pageCount}",
            color = AppTheme.colors.onPrimary
        )

        BodyMediumText(
            modifier = Modifier
                .animateClickable {
                    if (pagerState.currentPage == questions.size - 1) {
                        onAction(EnglishLevelUiEvent.OnCalculateLevelBtnClick)
                    } else {
                        if (hasNext) {
                            cScope.launch {
                                pagerState.animateScrollToPage(page = pagerState.currentPage + 1)
                            }
                        }
                    }
                },
            color = if (hasNext) AppTheme.colors.onPrimary
            else AppTheme.colors.outline,
            text = if (pagerState.currentPage == questions.size - 1) {
                stringResource(Res.string.english_level_screen_btn)
            } else {
                stringResource(Res.string.next)
            }
        )

    }
}
