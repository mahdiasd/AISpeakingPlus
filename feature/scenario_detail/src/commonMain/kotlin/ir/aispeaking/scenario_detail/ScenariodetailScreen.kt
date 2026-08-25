package ir.aispeaking.scenario_detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.level.LanguageGroupLevel
import ir.aispeaking.domain.model.scenario.ScenarioDetail
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.scenario_detail.component.DescriptionContent
import ir.aispeaking.scenario_detail.component.TasksContent
import ir.aispeaking.scenario_detail.component.TopContent
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.btn_when_ready_to_play
import ir.aispeaking.sharedui.btn_when_user_do_not_have_subscription
import ir.aispeaking.sharedui.btn_when_user_not_logged_in
import ir.aispeaking.sharedui.ic_arrow_right
import ir.aispeaking.sharedui.ui.core.button.AppCompactButton
import ir.aispeaking.sharedui.ui.core.error.ErrorContent
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.loading.PageLoading
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import ir.aispeaking.utils.constant.AppConstant
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ScenarioDetailScreen(
    scenarioId: String,
    scenarioImageUrl: String,
    scenarioTitle: String,
    isChallenge: Boolean,
    vm: ScenarioDetailViewModel = koinViewModel {
        parametersOf(
            scenarioId,
            scenarioImageUrl,
            scenarioTitle,
            if (isChallenge) ScreenMode.Challenge else ScreenMode.Scenario
        )
    },
    navigateToLogin: () -> Unit,
    navigateToPurchase: () -> Unit,
    navigateToChat: (LanguageGroupLevel) -> Unit,
    navigateBack: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    OnResume {
        if (AppConstant.needToRefreshScenarioDetail)
            vm.onTriggerEvent(ScenarioDetailUiEvent.OnRetry)
    }

    if (!uiState.isPageLoading && uiState.scenarioDetail == null) {
        ErrorContent(
            modifier = Modifier.baseModifier(0.dp),
            onRetry = { vm.onTriggerEvent(ScenarioDetailUiEvent.OnRetry) })
    } else {
        ScenarioDetailScreenContent(
            modifier = Modifier.baseModifier(),
            imageUrl = uiState.scenarioImageUrl,
            scenarioDetail = uiState.scenarioDetail,
            isPageLoading = uiState.isPageLoading,
            user = uiState.user,
            levelGroups = uiState.languageGroupLevels,
            onBack = navigateBack,
            onAction = { vm.onTriggerEvent(it) }
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is ScenarioDetailUiNavigation.ToBack -> navigateBack()
            is ScenarioDetailUiNavigation.ToLogin -> navigateToLogin()
            is ScenarioDetailUiNavigation.ToSubscription -> navigateToPurchase()
            is ScenarioDetailUiNavigation.ToChat -> navigateToChat((uiNavigation as ScenarioDetailUiNavigation.ToChat).languageGroupLevel)
        }
    }
}


@Composable
fun ScenarioDetailScreenContent(
    modifier: Modifier,
    imageUrl: String?,
    scenarioDetail: ScenarioDetail?,
    isPageLoading: Boolean = false,
    user: User? = null,
    onAction: OnAction,
    levelGroups: ImmutableList<LanguageGroupLevel>?,
    onBack: () -> Unit = {},
) {

    Scaffold(
        containerColor = AppTheme.colors.surface,
        modifier = modifier,
        topBar = {
            AnimatedVisibility(scenarioDetail != null) {
                TopContent(
                    modifier = Modifier.fillMaxWidth(),
                    imageUrl = imageUrl,
                    onAction = onAction,
                    aiAvatar = scenarioDetail?.scenario?.aiAvatar,
                    levelGroups = levelGroups,
                    onBack = onBack,
                    points = scenarioDetail?.scenario?.score.toString()
                )
            }
        },
        content = {
            AnimatedContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(it),
                targetState = !isPageLoading && scenarioDetail?.scenario != null
            ) { showScenarioContent ->
                when (showScenarioContent) {
                    true -> {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .clipScrollableContainer(Orientation.Vertical)
                                .padding(PaddingValues(top = 16.dp, bottom = 70.dp)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
                        ) {
                            val boxModifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .shadow(1.dp, shape = AppTheme.shapes.roundMedium)
                                .background(AppTheme.colors.surface, shape = AppTheme.shapes.roundMedium)
                                .border(color = AppTheme.colors.surfaceContainerHighest, width = 1.dp, shape = AppTheme.shapes.roundMedium)
                                .padding(16.dp)

                            DescriptionContent(
                                modifier = boxModifier,
                                scenario = scenarioDetail!!.scenario
                            )

                            TasksContent(
                                modifier = boxModifier,
                                scenario = scenarioDetail.scenario
                            )
                        }
                    }

                    false -> {
                        PageLoading(
                            modifier = Modifier
                                .fillMaxSize(),
                        )
                    }
                }

            }
        },
        floatingActionButton = {
            AnimatedVisibility(scenarioDetail != null)
            {
                AppCompactButton(
                    modifier = Modifier,
                    containerColor = AppTheme.colors.primary,
                    textColor = AppTheme.colors.onPrimary,
                    borderWidth = 1.dp,
                    borderColor = AppTheme.colors.outlineVariant,
                    shape = AppTheme.shapes.roundSmall,
                    text = when {
                        user == null -> Res.string.btn_when_user_not_logged_in
                        scenarioDetail?.userHaveSubscription != true -> Res.string.btn_when_user_do_not_have_subscription
                        else -> Res.string.btn_when_ready_to_play
                    },
                    icon = {
                        AppIcon(
                            icon = Res.drawable.ic_arrow_right,
                            tint = AppTheme.colors.onPrimary,
                            size = 16.dp
                        )
                    },
                    onClick = { onAction(ScenarioDetailUiEvent.OnPlayClick) }
                )
            }
        }
    )
}


@PreviewLightDark
@Composable
private fun ScenarioDetailPreview() {
    AppTheme {
        ScenarioDetailScreenContent(
            modifier = Modifier.baseModifier(),
            imageUrl = "",
            scenarioDetail = FakeData.provideScenarioDetail(),
            isPageLoading = false,
            onAction = { },
            levelGroups = LanguageGroupLevel.entries,
        )
    }
}

@PreviewLightDark
@Composable
private fun ScenarioDetailLoadingPreview() {
    AppTheme {
        ScenarioDetailScreenContent(
            modifier = Modifier.baseModifier(),
            imageUrl = "",
            scenarioDetail = null,
            isPageLoading = true,
            onAction = { },
            levelGroups = LanguageGroupLevel.entries,
        )
    }
}



