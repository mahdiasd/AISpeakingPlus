package ir.aispeaking.scenarios

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.domain.model.home.Home
import ir.aispeaking.domain.model.home.HomeData
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.scenarios.component.BannerItem
import ir.aispeaking.scenarios.component.CategoriesItem
import ir.aispeaking.scenarios.component.ScenariosItem
import ir.aispeaking.scenarios.component.WelcomeMessageDialog
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.is_search
import ir.aispeaking.sharedui.scenarios_title
import ir.aispeaking.sharedui.ui.core.guide.GuideDialog
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.list.SwipeList
import ir.aispeaking.sharedui.ui.core.toolbar.AppToolbar
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScenariosScreen(
    vm: ScenariosViewModel = koinViewModel(),
    navigateToSearch: (Category?) -> Unit,
    navigateToScenarioDetail: (ScenarioSummary) -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    ScenariosScreenContent(
        modifier = Modifier.fillMaxSize(),
        homeItems = uiState.homeItems,
        isRefreshing = uiState.isRefreshing,
        errorHappened = uiState.errorHappened,
        onAction = { vm.onTriggerEvent(it) },
    )

    UiMessageScreen(shared = vm.uiMessage)

    if (uiState.guideList.isNotEmpty()) {
        GuideDialog(
            modifier = Modifier.fillMaxWidth(),
            onDismiss = { vm.onTriggerEvent(ScenariosUiEvent.SetGuideRead) },
            list = uiState.guideList
        )
    }

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is ScenariosUiNavigation.ToSearch -> {
                navigateToSearch((uiNavigation as ScenariosUiNavigation.ToSearch).category)
            }

            is ScenariosUiNavigation.ToScenarioDetail -> {
                navigateToScenarioDetail((uiNavigation as ScenariosUiNavigation.ToScenarioDetail).scenarioSummary)
            }
        }
    }

    if (uiState.welcomeMessage?.isReadByUser != null && !uiState.welcomeMessage.isReadByUser) {
        WelcomeMessageDialog(
            welcomeMessage = uiState.welcomeMessage,
            onDismiss = {
                vm.onTriggerEvent(ScenariosUiEvent.OnWelcomeMessageDismiss(it))
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenariosScreenContent(
    modifier: Modifier = Modifier,
    onAction: OnAction,
    isRefreshing: Boolean = false,
    errorHappened: Boolean = false,
    homeItems: ImmutableList<Home>,
) {
    Column(
        modifier = modifier,
    ) {
        AppToolbar(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            title = stringResource(Res.string.scenarios_title),
            leftContent = null,
            rightContent = {
                AppIcon(
                    icon = Res.drawable.is_search,
                    modifier = Modifier.animateClickable { onAction(ScenariosUiEvent.OnSearchClick) },
                )
            },
        )
        SwipeList(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .weight(1f)
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            key = { index -> homeItems[index].hashCode() },
            isRefreshing = isRefreshing,
            isLoadMore = false,
            listSize = homeItems.size,
            errorHappened = errorHappened,
            contentAlignment = Alignment.TopCenter,
            onRefresh = { onAction(ScenariosUiEvent.OnRefreshList) },
            onLoadMore = {},
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
            screenToShow = { index, modifier ->
                when (homeItems[index].data) {
                    is HomeData.BannerData -> {
                        BannerItem(
                            modifier = modifier,
                            banner = (homeItems[index].data as HomeData.BannerData).banner,
                            onClick = { banner -> onAction(ScenariosUiEvent.OnBannerClick(banner)) }
                        )
                    }

                    is HomeData.CategoriesData -> {
                        CategoriesItem(
                            modifier = modifier,
                            title = homeItems[index].title ?: "",
                            categories = (homeItems[index].data as HomeData.CategoriesData).categories,
                            onCategoryClick = { category ->
                                onAction(ScenariosUiEvent.OnCategoryClick(category))
                            }
                        )
                    }

                    is HomeData.ScenariosData -> {
                        ScenariosItem(
                            modifier = modifier,
                            title = homeItems[index].title ?: "",
                            scenarios = (homeItems[index].data as HomeData.ScenariosData).scenarios,
                            onScenarioClick = { scenarioSummary ->
                                onAction(ScenariosUiEvent.OnScenarioClick(scenarioSummary))
                            }
                        )
                    }
                }
            }
        )

    }
}


@PreviewLightDark
@Composable
private fun ScenariosPreview() {
    AppTheme {
    }
}


