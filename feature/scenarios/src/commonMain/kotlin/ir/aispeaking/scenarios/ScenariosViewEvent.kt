package ir.aispeaking.scenarios

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.banner.Banner
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.domain.model.home.Home
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList

@Stable
data class ScenariosUiState(
    val isRefreshing: Boolean = false,
    val errorHappened: Boolean = false,
    val homeItems: ImmutableList<Home> = immutableListOf(),
    val guideList: ImmutableList<GuideModel> = ir.aispeaking.utils.immutableListOf(),
    val welcomeMessage: WelcomeMessage? = null
) : UiState


sealed class ScenariosUiEvent : UiEvent {
    data class OnCategoryClick(val category: Category) : ScenariosUiEvent()
    data class OnScenarioClick(val scenario: ScenarioSummary) : ScenariosUiEvent()
    data class OnBannerClick(val banner: Banner) : ScenariosUiEvent()
    data object OnSearchClick : ScenariosUiEvent()
    data object OnRefreshList : ScenariosUiEvent()
    data object SetGuideRead : ScenariosUiEvent()
    data class OnWelcomeMessageDismiss(val dontShowAgain: Boolean) : ScenariosUiEvent()

}

sealed class ScenariosUiNavigation : UiNavigation {
    data class ToSearch(val category: Category?) : ScenariosUiNavigation()
    data class ToScenarioDetail(val scenarioSummary: ScenarioSummary) : ScenariosUiNavigation()
}

typealias OnAction = (ScenariosUiEvent) -> Unit
