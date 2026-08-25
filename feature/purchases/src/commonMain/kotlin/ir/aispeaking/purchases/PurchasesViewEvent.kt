package ir.aispeaking.purchases

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState

@Stable
data class PurchasesUiState(
    val purchasePaging: Paging<Purchase> = Paging(immutableListOf()),
) : UiState


sealed class PurchasesUiEvent : UiEvent {
    data object OnBackClick : PurchasesUiEvent()
    data object OnRefresh : PurchasesUiEvent()
    data object OnLoadMore : PurchasesUiEvent()
}

sealed class PurchasesUiNavigation : UiNavigation {
    data object ToBack : PurchasesUiNavigation()
}

typealias OnAction = (PurchasesUiEvent) -> Unit



