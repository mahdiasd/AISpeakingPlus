package ir.aispeaking.competition

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.domain.model.user.UserSummary
import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList

@Stable
data class CompetitionUiState(
    val isPageLoading: Boolean = true,
    val acceptableBtnLoading: Boolean = false,
    val users: ImmutableList<UserSummary>? = immutableListOf(),
    val user: User? = null,

    val showTranslateDialog: Boolean = false,
    /**
     * Active word ↓*/
    val userSelectedWordIndex: Int? = null,
    val dailyWord: DailyWord? = null,
    val notFoundActiveWord: Boolean = false,

    /**
     * Challenge ↓
     * */
    val challengeSummary: ChallengeSummary? = null
) : UiState


sealed class CompetitionUiEvent : UiEvent {
    data object OnRetry : CompetitionUiEvent()
    data object OnNavigateToChallenge : CompetitionUiEvent()
    data object OnNavigateToLogin : CompetitionUiEvent()
    data object OnSendAnswer : CompetitionUiEvent()
    data object SyncUserWithServer : CompetitionUiEvent()
    data class OnWordSelectedAnswer(val index: Int) : CompetitionUiEvent()
    data class OnShowTranslateDialog(val show: Boolean) : CompetitionUiEvent()
}

sealed class CompetitionUiNavigation : UiNavigation {
    data object ToLogin : CompetitionUiNavigation()
    data class ToChallenge(val challengeSummary: ChallengeSummary) : CompetitionUiNavigation()
}

typealias OnAction = (CompetitionUiEvent) -> Unit

