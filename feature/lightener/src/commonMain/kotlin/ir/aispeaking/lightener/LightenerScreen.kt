package ir.aispeaking.lightener

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.translate.Translation
import ir.aispeaking.lightener.component.LightenerItem
import ir.aispeaking.lightener.component.LightenerToolbar
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.delete
import ir.aispeaking.sharedui.delete_dialog_message
import ir.aispeaking.sharedui.delete_dialog_title
import ir.aispeaking.sharedui.empty_lightener
import ir.aispeaking.sharedui.not_now
import ir.aispeaking.sharedui.ui.core.dialog.MessageDialog
import ir.aispeaking.sharedui.ui.core.list.SwipeList
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.core.unauthorized.UnauthorizedContent
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.utils.lifecycle.OnResume
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LightenerScreen(
    vm: LightenerViewModel = koinViewModel(),
    navigateToLogin: () -> Unit,
    navigateBack: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    OnResume {
        vm.onTriggerEvent(LightenerUiEvent.CheckForFetching)
    }

    if (uiState.user != null) {
        LightenerScreenContent(
            modifier = Modifier.fillMaxSize(),
            paging = uiState.translationPaging,
            isExpandedSearch = uiState.isExpandedSearch,
            searchText = uiState.searchText,
            deletingId = uiState.deletingId,
            onAction = { vm.onTriggerEvent(it) }
        )
    } else {
        UnauthorizedContent(
            modifier = Modifier.baseModifier(),
            navigateToLogin = navigateToLogin
        )
    }

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is LightenerUiNavigation.ToBack -> navigateBack()
        }
    }

    if (!uiState.deleteDialogUid.isNullOrEmpty()) {
        MessageDialog(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.colors.surfaceContainerLow, AppTheme.shapes.roundMedium)
                .padding(16.dp),
            title = stringResource(Res.string.delete_dialog_title),
            message = stringResource(Res.string.delete_dialog_message),
            positiveText = Res.string.not_now,
            negativeText = Res.string.delete,
            onPositive = {
                vm.onTriggerEvent(LightenerUiEvent.OnShowDeleteDialog(null))
            },
            onNegative = {
                vm.onTriggerEvent(LightenerUiEvent.DeleteTranslation(uiState.deleteDialogUid))
                vm.onTriggerEvent(LightenerUiEvent.OnShowDeleteDialog(null))
            },
            onDismiss = {
                vm.onTriggerEvent(LightenerUiEvent.OnShowDeleteDialog(null))
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightenerScreenContent(
    modifier: Modifier = Modifier,
    paging: Paging<Translation>,
    isExpandedSearch: Boolean,
    searchText: String,
    deletingId: String,
    onAction: OnAction,
) {
    val isShowingAllTranslations by remember(paging.content) { derivedStateOf { paging.content.all { it.showTranslate } } }

    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        topBar = {
            LightenerToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                searchText = searchText,
                isSearchExpanded = isExpandedSearch,
                isShowAllTranslations = isShowingAllTranslations,
                lightenerSize = paging.content.size,
                onAction = onAction
            )
        }
    ) {
        SwipeList(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            isRefreshing = paging.isRefreshing,
            isLoadMore = paging.isLoadMore,
            listSize = paging.content.size,
            onRefresh = { onAction(LightenerUiEvent.OnRefresh) },
            onLoadMore = { onAction(LightenerUiEvent.OnLoadMore) },
            emptyText = Res.string.empty_lightener,
            errorHappened = paging.apiErrorHappened,
            key = { index ->
                paging.content[index].uid
            },
        ) { index, modifier ->
            val translation by remember(index, paging.content) { mutableStateOf(paging.content[index]) }

            LightenerItem(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .padding(horizontal = 8.dp),
                translation = translation,
                isRemoveLoading = deletingId == translation.uid,
                onAction = onAction
            )

            if (index != paging.content.lastIndex && !translation.expanded)
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(1.dp)
                        .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium),
                )
        }
    }
}


@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
    }
}


