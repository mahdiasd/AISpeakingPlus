package ir.aispeaking.search

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiState
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import kotlinx.collections.immutable.ImmutableList

@Stable
data class SearchUiState(
    val isBtnLoading: Boolean = false,
    val categoriesLoading: Boolean = false,
    val category: Category? = null,
    val searchText: String = "",
    val scenarioPaging: Paging<ScenarioSummary> = Paging(immutableListOf(), isRefreshing = true),
    val allCategories: ImmutableList<Category>? = null,
    val showCategoriesDialog: Boolean = false,
) : UiState


sealed class SearchUiEvent : UiEvent {
    data object OnBackClick : SearchUiEvent()
    data object OnRefresh : SearchUiEvent()
    data object OnGetCategories : SearchUiEvent()
    data object OnLoadMore : SearchUiEvent()
    data object OnSearch : SearchUiEvent()
    data class OnSearchTextChange(val text: String) : SearchUiEvent()
    data class OnCategoriesDialog(val show: Boolean) : SearchUiEvent()
    data class OnCategorySelected(val category: Category) : SearchUiEvent()
    data class OnScenarioSelected(val scenario: ScenarioSummary) : SearchUiEvent()
}

sealed class SearchUiNavigation : UiNavigation {
    data object ToBack : SearchUiNavigation()
    data class ToScenario(val scenario: ScenarioSummary) : SearchUiNavigation()
}

typealias OnAction = (SearchUiEvent) -> Unit


