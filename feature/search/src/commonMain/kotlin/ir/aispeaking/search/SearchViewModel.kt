package ir.aispeaking.search

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.paging.addMore
import ir.aispeaking.domain.model.paging.errorHappened
import ir.aispeaking.domain.model.paging.firstPage
import ir.aispeaking.domain.model.paging.nextPage
import ir.aispeaking.domain.usecase.category.GetCategoriesUseCase
import ir.aispeaking.domain.usecase.scenario.GetScenariosUseCase
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class SearchViewModel(
    private val categoryId: String?,
    private val getScenariosUseCase: GetScenariosUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
) : BaseViewModel<SearchUiState, SearchUiEvent>() {

    init {
        onTriggerEvent(SearchUiEvent.OnGetCategories)
    }

    override fun createInitialState() = SearchUiState()

    override fun onTriggerEvent(event: SearchUiEvent) {
        when (event) {
            is SearchUiEvent.OnBackClick -> {
                setUiNavigation(SearchUiNavigation.ToBack)
            }

            is SearchUiEvent.OnLoadMore -> {
                if (!currentState.scenarioPaging.isLast) {
                    setState { copy(scenarioPaging = scenarioPaging.nextPage()) }
                    getScenarios()
                }
            }

            is SearchUiEvent.OnRefresh -> {
                setState { copy(scenarioPaging = scenarioPaging.firstPage()) }
                getScenarios()
            }

            is SearchUiEvent.OnSearchTextChange -> {
                setState { copy(searchText = event.text) }
            }

            is SearchUiEvent.OnSearch -> {
                setState { copy(scenarioPaging = scenarioPaging.firstPage()) }
                getScenarios()
            }

            is SearchUiEvent.OnGetCategories -> {
                getCategories()
            }

            is SearchUiEvent.OnCategoriesDialog -> {
                if (currentState.allCategories.isNullOrEmpty())
                    getCategories()
                else
                    setState { copy(showCategoriesDialog = event.show) }
            }

            is SearchUiEvent.OnCategorySelected -> {
                setState {
                    copy(
                        showCategoriesDialog = false,
                        category = if (currentState.category?.id == event.category.id) null else event.category,
                        scenarioPaging = scenarioPaging.firstPage()
                    )
                }
                getScenarios()
            }

            is SearchUiEvent.OnScenarioSelected -> {
                setUiNavigation(SearchUiNavigation.ToScenario(event.scenario))
            }
        }
    }

    private fun getScenarios() {
        viewModelScope.launch {
            getScenariosUseCase(
                searchText = currentState.searchText.ifEmpty { null },
                categoryId = currentState.category?.id ?: categoryId,
                page = currentState.scenarioPaging.page,
            ).collect {
                it.onSuccess {
                    setState { copy(scenarioPaging = scenarioPaging.addMore(it)) }
                }.onFailure { apiError ->
                    setState { copy(scenarioPaging = scenarioPaging.errorHappened()) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun getCategories() {
        setState { copy(categoriesLoading = true) }
        viewModelScope.launch {
            getCategoriesUseCase(
            ).collect {
                it.onSuccess { categories ->
                    setState {
                        copy(
                            allCategories = categories.toImmutableList(),
                            categoriesLoading = false
                        )
                    }
                    if (categoryId != null)
                    {
                        setState { copy(category = categories.find { it.id == categoryId }) }
                    }
                }.onFailure { apiError ->
                    setState { copy(categoriesLoading = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

}


