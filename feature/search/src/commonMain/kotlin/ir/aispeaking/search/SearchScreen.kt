@file:OptIn(ExperimentalMaterial3Api::class)

package ir.aispeaking.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.search.component.CategoriesDialog
import ir.aispeaking.search.component.SearchTopBar
import ir.aispeaking.sharedui.ui.core.list.SwipeGrid
import ir.aispeaking.sharedui.ui.core.scenario.ScenarioSummaryItem
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme

import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SearchScreen(
    categoryId: String?,
    vm: SearchViewModel = koinViewModel { parametersOf(categoryId) },
    navigateBack: () -> Unit,
    navigateScenario: (ScenarioSummary) -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    SearchScreenContent(
        modifier = Modifier.baseModifier(),
        onAction = { vm.onTriggerEvent(it) },
        scenarioPaging = uiState.scenarioPaging,
        category = uiState.category,
        searchText = uiState.searchText
    )

    UiMessageScreen(shared = vm.uiMessage)

    if (uiState.showCategoriesDialog && !uiState.allCategories.isNullOrEmpty()) {
        CategoriesDialog(
            modifier = Modifier.fillMaxWidth(),
            categories = uiState.allCategories,
            selectedCategory = uiState.category,
            onCategorySelected = { selectedCategory ->
                vm.onTriggerEvent(SearchUiEvent.OnCategorySelected(selectedCategory))
            },
            onDismiss = {
                vm.onTriggerEvent(SearchUiEvent.OnCategoriesDialog(false))
            }
        )
    }

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is SearchUiNavigation.ToBack -> navigateBack()
            is SearchUiNavigation.ToScenario -> navigateScenario((uiNavigation as SearchUiNavigation.ToScenario).scenario)
        }
    }
}


@Composable
fun SearchScreenContent(
    modifier: Modifier = Modifier,
    scenarioPaging: Paging<ScenarioSummary>,
    category: Category? = null,
    searchText: String,
    onAction: OnAction
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        topBar = {
            SearchTopBar(
                onBackClick = { onAction(SearchUiEvent.OnBackClick) },
                category = category,
                searchText = searchText,
                onAction = onAction
            )
        }
    ) {
        SwipeGrid(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp, horizontal = 16.dp)
                .padding(it),
            isRefreshing = scenarioPaging.isRefreshing,
            isLoadMore = scenarioPaging.isLoadMore,
            listSize = scenarioPaging.content.size,
            errorHappened = scenarioPaging.apiErrorHappened,
            onRefresh = { onAction(SearchUiEvent.OnRefresh) },
            onLoadMore = { onAction(SearchUiEvent.OnLoadMore) },
            key = { index -> scenarioPaging.content[index].id },
            columns = GridCells.Fixed(2),
            horizontalAlignment = Arrangement.spacedBy(16.dp)
        ) { index, modifier ->
            ScenarioSummaryItem(
                modifier = modifier
                    .height(125.dp),
                item = scenarioPaging.content[index],
                onScenarioClick = { onAction(SearchUiEvent.OnScenarioSelected(scenarioPaging.content[index])) }
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun SearchPreview() {
    AppTheme {
        SearchScreenContent(
            modifier = Modifier.baseModifier(),
            scenarioPaging = Paging(immutableListOf()),
            category = null,
            searchText = ""
        ) { }
    }
}



