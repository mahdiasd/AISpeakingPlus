package ir.aispeaking.scenario_detail

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.level.LanguageGroupLevel
import ir.aispeaking.domain.model.scenario.ScenarioDetail
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList

@Stable
data class ScenarioDetailUiState(
    val isPageLoading: Boolean = true,
    val scenarioTitle: String = "",
    val scenarioImageUrl: String? = null,

    val scenarioDetail: ScenarioDetail? = null,
    val languageGroupLevels: ImmutableList<LanguageGroupLevel>? = null,
    val user: User? = null,

    val screenMode: ScreenMode = ScreenMode.Scenario
) : UiState

sealed class ScreenMode {
    data object Challenge : ScreenMode()
    data object Scenario : ScreenMode()
}

sealed class ScenarioDetailUiEvent : UiEvent {
    data object OnBackClick : ScenarioDetailUiEvent()
    data object OnPlayClick : ScenarioDetailUiEvent()
    data class OnLanguageGroupLevel(val languageGroupLevel: LanguageGroupLevel) : ScenarioDetailUiEvent()
    data object OnRetry : ScenarioDetailUiEvent()
}

sealed class ScenarioDetailUiNavigation : UiNavigation {
    data object ToBack : ScenarioDetailUiNavigation()
    data object ToLogin : ScenarioDetailUiNavigation()
    data object ToSubscription : ScenarioDetailUiNavigation()
    data class ToChat(val languageGroupLevel: LanguageGroupLevel) : ScenarioDetailUiNavigation()
}

typealias OnAction = (ScenarioDetailUiEvent) -> Unit


