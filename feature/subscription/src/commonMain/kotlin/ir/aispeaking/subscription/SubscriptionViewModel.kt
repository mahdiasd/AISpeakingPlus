package ir.aispeaking.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.usecase.stage.CheckSubscriptionStatusUseCase
import ir.aispeaking.domain.usecase.stage.GetSubscriptionPlansUseCase
import ir.aispeaking.domain.usecase.stage.SubscribePlanUseCase
import ir.aispeaking.domain.usecase.stage.ValidatePromoCodeUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class SubscriptionViewModel(
    private val getSubscriptionPlansUseCase: GetSubscriptionPlansUseCase,
    private val checkSubscriptionStatusUseCase: CheckSubscriptionStatusUseCase,
    private val subscribePlanUseCase: SubscribePlanUseCase,
    private val validatePromoCodeUseCase: ValidatePromoCodeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SubscriptionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        loadData()
    }

    fun processIntent(intent: SubscriptionIntent) {
        when (intent) {
            is SubscriptionIntent.LoadData -> loadData()
            is SubscriptionIntent.SelectPlan -> selectPlan(intent.planId)
            is SubscriptionIntent.OnPromoCodeChanged -> onPromoCodeChanged(intent.code)
            is SubscriptionIntent.ApplyPromoCode -> applyPromoCode()
            is SubscriptionIntent.PurchaseSelectedPlan -> purchaseSelectedPlan()
            is SubscriptionIntent.DismissError -> dismissError()
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 1. Fetch available plans
            val plansResult = getSubscriptionPlansUseCase()
            val (loadedPlans, plansError) = when (plansResult) {
                is DataResult.Success -> plansResult.data to null
                is DataResult.Failure -> {
                    val errorMsg = plansResult.appError.getErrorMessage().ifBlank {
                        "خطا در دریافت لیست پلن‌های اشتراک. لطفاً اتصال اینترنت خود را بررسی کنید."
                    }
                    emptyList<SubscriptionPlan>() to errorMsg
                }
            }

            // 2. Fetch current status
            val statusResult = checkSubscriptionStatusUseCase()
            val currentStatus = when (statusResult) {
                is DataResult.Success -> statusResult.data
                is DataResult.Failure -> null
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    plans = loadedPlans,
                    selectedPlanId = if (loadedPlans.any { p -> p.id == it.selectedPlanId }) it.selectedPlanId else (loadedPlans.getOrNull(1)?.id ?: loadedPlans.firstOrNull()?.id ?: ""),
                    errorMessage = plansError,
                    currentStatus = currentStatus
                )
            }
        }
    }

    private fun selectPlan(planId: String) {
        _uiState.update { it.copy(selectedPlanId = planId) }
    }

    private fun onPromoCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                promoCodeInput = code,
                promoFeedbackMessage = null,
                isPromoError = false
            )
        }
    }

    private fun applyPromoCode() {
        val rawCode = _uiState.value.promoCodeInput.trim()
        if (rawCode.isBlank()) {
            _uiState.update {
                it.copy(
                    promoFeedbackMessage = "لطفاً کد تخفیف را وارد کنید",
                    isPromoError = true
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = validatePromoCodeUseCase(rawCode)
            when (result) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appliedDiscountPercent = result.data.discountPercent,
                            promoFeedbackMessage = result.data.message,
                            isPromoError = false
                        )
                    }
                }
                is DataResult.Failure -> {
                    val errorMsg = result.appError.getErrorMessage().ifBlank {
                        "کد تخفیف واردشده نامعتبر یا منقضی است"
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appliedDiscountPercent = 0,
                            promoFeedbackMessage = errorMsg,
                            isPromoError = true
                        )
                    }
                }
            }
        }
    }

    private fun purchaseSelectedPlan() {
        val state = _uiState.value
        val plan = state.selectedPlan ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isPurchasing = true, errorMessage = null) }

            val result = subscribePlanUseCase(
                planId = plan.id,
                promoCode = if (state.appliedDiscountPercent > 0) state.promoCodeInput.trim() else null
            )

            when (result) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            currentStatus = result.data
                        )
                    }
                    _effect.send(
                        SubscriptionEffect.SubscriptionActivated(
                            remainingDays = result.data.remainingDays,
                            planTitle = plan.titleFa
                        )
                    )
                }
                is DataResult.Failure -> {
                    val errorMsg = "خطا در فعال‌سازی اشتراک. لطفاً اتصال اینترنت و وضعیت ورود خود را بررسی کنید."
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            errorMessage = errorMsg
                        )
                    }
                    _effect.send(SubscriptionEffect.ShowSnackbar(errorMsg))
                }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
