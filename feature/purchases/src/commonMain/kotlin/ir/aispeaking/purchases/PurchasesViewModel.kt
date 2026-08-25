package ir.aispeaking.purchases

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.paging.addMore
import ir.aispeaking.domain.model.paging.errorHappened
import ir.aispeaking.domain.model.paging.firstPage
import ir.aispeaking.domain.model.paging.nextPage
import ir.aispeaking.domain.usecase.purchase.GetPurchasesByUserIdUseCase
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class PurchasesViewModel(
    private val getPurchasesUseCase: GetPurchasesByUserIdUseCase,
) : BaseViewModel<PurchasesUiState, PurchasesUiEvent>() {

    init {
        onTriggerEvent(PurchasesUiEvent.OnRefresh)
    }

    override fun createInitialState() = PurchasesUiState()

    override fun onTriggerEvent(event: PurchasesUiEvent) {
        when (event) {
            PurchasesUiEvent.OnBackClick -> {
                setUiNavigation(PurchasesUiNavigation.ToBack)
            }

            PurchasesUiEvent.OnLoadMore -> {
                if (!currentState.purchasePaging.isLast) {
                    setState { copy(purchasePaging = purchasePaging.nextPage()) }
                    getPurchases()
                }
            }

            PurchasesUiEvent.OnRefresh -> {
                setState { copy(purchasePaging = purchasePaging.firstPage()) }
                getPurchases()
            }
        }
    }

    private fun getPurchases() {
        viewModelScope.launch {
            getPurchasesUseCase(
                page = currentState.purchasePaging.page,
            ).collect {
                it.onSuccess {
                    setState { copy(purchasePaging = purchasePaging.addMore(it)) }
                }.onFailure { apiError ->
                    setState { copy(purchasePaging = purchasePaging.errorHappened()) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }


}



