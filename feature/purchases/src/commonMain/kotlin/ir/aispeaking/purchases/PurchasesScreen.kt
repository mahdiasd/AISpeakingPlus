@file:OptIn(ExperimentalMaterial3Api::class)

package ir.aispeaking.purchases

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.purchases.component.PurchaseItem
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.purchase_screen_title
import ir.aispeaking.sharedui.ui.core.list.SwipeList
import ir.aispeaking.sharedui.ui.core.toolbar.AppToolbar
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PurchasesScreen(
    vm: PurchasesViewModel = koinViewModel(),
    navigateBack: () -> Unit,
) {
    val uiState = vm.uiState.collectAsState().value
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)

    PurchasesScreenContent(
        modifier = Modifier.fillMaxSize(),
        onAction = { vm.onTriggerEvent(it) },
        purchasePaging = uiState.purchasePaging,
    )

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is PurchasesUiNavigation.ToBack -> navigateBack()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreenContent(
    modifier: Modifier = Modifier,
    purchasePaging: Paging<Purchase>,
    onAction: OnAction,
) {
    Scaffold(
        modifier = modifier,
        containerColor = AppTheme.colors.surface,
        topBar = {
            AppToolbar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .statusBarsPadding(),
                title = stringResource(Res.string.purchase_screen_title),
                onLeftIconClick = { onAction(PurchasesUiEvent.OnBackClick) }
            )
        }) {

        SwipeList(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(vertical = 16.dp, horizontal = 16.dp)
                .padding(it),
            isRefreshing = purchasePaging.isRefreshing,
            isLoadMore = purchasePaging.isLoadMore,
            errorHappened = purchasePaging.apiErrorHappened,
            listSize = purchasePaging.content.size,
            onRefresh = { onAction(PurchasesUiEvent.OnRefresh) },
            onLoadMore = { onAction(PurchasesUiEvent.OnLoadMore) },
            key = { index -> purchasePaging.content[index].id ?: index },
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { index, modifier ->
            PurchaseItem(
                modifier = modifier.height(140.dp),
                item = purchasePaging.content[index],
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun PurchasesPreview() {
    AppTheme {
        PurchasesScreenContent(
            modifier = Modifier.baseModifier(),
            purchasePaging = Paging(FakeData.providePurchases())
        ) { }
    }
}




