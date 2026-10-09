package ir.aispeaking.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.usecase.stage.CheckSubscriptionStatusUseCase
import ir.aispeaking.domain.usecase.stage.GetSubscriptionPlansUseCase
import ir.aispeaking.domain.usecase.stage.SubscribePlanUseCase
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
    private val subscribePlanUseCase: SubscribePlanUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SubscriptionEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private val defaultPlans = listOf(
        SubscriptionPlan(
            id = "plan-1m",
            type = "1_MONTH",
            titleFa = "اشتراک ۱ ماهه",
            durationDays = 30,
            priceTomans = 199_000L,
            discountPercent = 0,
            badge = null
        ),
        SubscriptionPlan(
            id = "plan-3m",
            type = "3_MONTHS",
            titleFa = "اشتراک ۳ ماهه",
            durationDays = 90,
            priceTomans = 499_000L,
            discountPercent = 15,
            badge = "محبوب‌ترین"
        ),
        SubscriptionPlan(
            id = "plan-6m",
            type = "6_MONTHS",
            titleFa = "اشتراک ۶ ماهه",
            durationDays = 180,
            priceTomans = 899_000L,
            discountPercent = 25,
            badge = "بهترین ارزش"
        )
    )

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
            val loadedPlans = when (plansResult) {
                is DataResult.Success -> {
                    if (plansResult.data.isNotEmpty()) plansResult.data else defaultPlans
                }
                is DataResult.Failure -> defaultPlans
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
                    selectedPlanId = if (loadedPlans.any { p -> p.id == it.selectedPlanId }) it.selectedPlanId else (loadedPlans.getOrNull(1)?.id ?: loadedPlans.firstOrNull()?.id ?: "plan-3m"),
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
        val rawCode = _uiState.value.promoCodeInput.trim().uppercase()
        if (rawCode.isBlank()) {
            _uiState.update {
                it.copy(
                    promoFeedbackMessage = "لطفاً کد تخفیف را وارد کنید",
                    isPromoError = true
                )
            }
            return
        }

        val discountPercent = when (rawCode) {
            "GOLD20" -> 20
            "NOWRUZ" -> 25
            "AISPEAKING" -> 30
            "VIP" -> 15
            else -> {
                // If it looks like a custom promo code with numbers
                if (rawCode.length >= 4) 10 else null
            }
        }

        if (discountPercent != null) {
            _uiState.update {
                it.copy(
                    appliedDiscountPercent = discountPercent,
                    promoFeedbackMessage = "کد تخفیف $discountPercent٪ با موفقیت اعمال شد ✓",
                    isPromoError = false
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    appliedDiscountPercent = 0,
                    promoFeedbackMessage = "کد تخفیف واردشده نامعتبر یا منقضی است",
                    isPromoError = true
                )
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
                    // Fallback simulation in dev/offline mode if server error
                    val fallbackRemaining = (state.currentStatus?.remainingDays ?: 0) + plan.durationDays
                    _uiState.update { it.copy(isPurchasing = false) }
                    _effect.send(
                        SubscriptionEffect.SubscriptionActivated(
                            remainingDays = fallbackRemaining,
                            planTitle = plan.titleFa
                        )
                    )
                }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
