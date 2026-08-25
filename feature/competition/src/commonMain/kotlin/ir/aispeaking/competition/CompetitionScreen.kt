package ir.aispeaking.competition

import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.competition.component.ChallengeSection
import ir.aispeaking.competition.component.DailyWordSection
import ir.aispeaking.competition.component.TopUsersSection
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.daily_challenge
import ir.aispeaking.sharedui.daily_word_title
import ir.aispeaking.sharedui.top_users_title
import ir.aispeaking.sharedui.ui.core.dialog.translate.TranslateDialog
import ir.aispeaking.sharedui.ui.core.error.ErrorContent
import ir.aispeaking.sharedui.ui.core.loading.PageLoading
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CompetitionScreen(
    vm: CompetitionViewModel = koinViewModel(),
    navigateToLogin: () -> Unit,
    navigateToScenarioDetail: (ChallengeSummary) -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    if (uiState.isPageLoading) {
        PageLoading(modifier = Modifier.fillMaxSize())
    } else if (uiState.users == null || uiState.dailyWord == null || uiState.challengeSummary == null) {
        ErrorContent(modifier = Modifier.fillMaxSize(), onRetry = { vm.onTriggerEvent(CompetitionUiEvent.OnRetry) })
    } else {
        CompetitionScreenContent(
            modifier = Modifier.fillMaxSize(),
            users = uiState.users,
            dailyWord = uiState.dailyWord,
            acceptableBtnLoading = uiState.acceptableBtnLoading,
            userSelectedWordIndex = uiState.userSelectedWordIndex,
            challengeSummary = uiState.challengeSummary,
            onAction = { vm.onTriggerEvent(it) },
            isLogin = uiState.user != null
        )
    }

    if (uiState.showTranslateDialog && !uiState.dailyWord?.word.isNullOrEmpty()) {
        TranslateDialog(
            modifier = Modifier,
            text = uiState.dailyWord?.word ?: "",
            onDismiss = { vm.onTriggerEvent(CompetitionUiEvent.OnShowTranslateDialog(false)) },
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is CompetitionUiNavigation.ToLogin -> navigateToLogin()
            is CompetitionUiNavigation.ToChallenge -> navigateToScenarioDetail((uiNavigation as CompetitionUiNavigation.ToChallenge).challengeSummary)
        }
    }
}


@Composable
fun CompetitionScreenContent(
    modifier: Modifier,
    onAction: OnAction,
    users: ImmutableList<UserSummary>,
    acceptableBtnLoading: Boolean,
    userSelectedWordIndex: Int?,
    dailyWord: DailyWord,
    challengeSummary: ChallengeSummary,
    isLogin: Boolean,
) {
    Column(
        modifier = modifier
            .clipScrollableContainer(Orientation.Vertical)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.Top)
    ) {
        TitleBoldText(
            text = stringResource(Res.string.top_users_title)
        )
        TopUsersSection(
            users = users,
        )
        VerticalSpace()
        TitleBoldText(
            text = stringResource(Res.string.daily_word_title)
        )

        DailyWordSection(
            dailyWord = dailyWord,
            acceptableBtnLoading = acceptableBtnLoading,
            userSelectedWordIndex = userSelectedWordIndex,
            isLogin = isLogin,
            onAction = onAction
        )

        VerticalSpace()
        TitleBoldText(
            text = stringResource(Res.string.daily_challenge)
        )
        ChallengeSection(
            modifier = Modifier.fillMaxWidth(),
            challengeSummary = challengeSummary,
            onAction = onAction
        )
    }
}


@LightDarkPreview
@Composable
private fun CompetitionPreview() {
    AppTheme {
        CompetitionScreenContent(
            modifier = Modifier.baseModifier(),
            onAction = {},
            users = FakeData.provideUsersSummary(),
            acceptableBtnLoading = false,
            userSelectedWordIndex = 0,
            dailyWord = FakeData.provideDailyWord(),
            challengeSummary = FakeData.provideChallengeSummary(),
            isLogin = false
        )
    }
}


