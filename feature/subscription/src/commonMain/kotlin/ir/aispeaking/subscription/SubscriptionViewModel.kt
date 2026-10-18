package ir.aispeaking.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.gateway.MarketBillingGateway
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.payment.MarketPurchaseResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.repository.PlatformMarketDetector
import ir.aispeaking.domain.usecase.stage.CheckSubscriptionStatusUseCase
import ir.aispeaking.domain.usecase.stage.CreateDirectCheckoutUseCase
import ir.aispeaking.domain.usecase.stage.GetSubscriptionPlansUseCase
import ir.aispeaking.domain.usecase.stage.SubscribePlanUseCase
import ir.aispeaking.domain.usecase.stage.ValidateMarketPromoUseCase
import ir.aispeaking.domain.usecase.stage.VerifyMarketPurchaseUseCase
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
    private val validateMarketPromoUseCase: ValidateMarketPromoUseCase,
    private val verifyMarketPurchaseUseCase: VerifyMarketPurchaseUseCase,
    private val createDirectCheckoutUseCase: CreateDirectCheckoutUseCase,
    private val marketDetector: PlatformMarketDetector,
    private val billingGateway: MarketBillingGateway
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
            val detectedMarket = try {
                marketDetector.detectMarket()
            } catch (_: Exception) {
                AppMarket.DIRECT_WEB
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    detectedMarket = detectedMarket
                )
            }

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

            // 3. Connect to market billing and recover unconsumed pending purchases (US5)
            if (detectedMarket == AppMarket.CAFE_BAZAAR || detectedMarket == AppMarket.MYKET) {
                launch {
                    recoverPendingPurchases(detectedMarket)
                }
            }
        }
    }

    private suspend fun recoverPendingPurchases(market: AppMarket) {
        try {
            val connected = billingGateway.startConnection()
            if (!connected) return

            val pendingPurchases = billingGateway.queryUnconsumedPurchases()
            for (purchase in pendingPurchases) {
                val verifyRes = verifyMarketPurchaseUseCase(
                    market = market.id,
                    planId = purchase.sku,
                    sku = purchase.sku,
                    purchaseToken = purchase.purchaseToken,
                    orderId = purchase.orderId,
                    developerPayload = purchase.developerPayload
                )
                if (verifyRes is DataResult.Success) {
                    billingGateway.consumePurchase(purchase.purchaseToken)
                    _uiState.update { it.copy(currentStatus = verifyRes.data) }
                }
            }
        } catch (_: Throwable) {
            // Background recovery should not disrupt UI
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
            val currentPlanId = _uiState.value.selectedPlanId
            val currentMarket = _uiState.value.detectedMarket

            val result = validateMarketPromoUseCase(
                promoCode = rawCode,
                planId = currentPlanId,
                market = currentMarket.id
            )

            when (result) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            appliedDiscountPercent = result.data.discountPercent,
                            dynamicDiscountToken = result.data.dynamicDiscountToken,
                            promoBonusDays = result.data.bonusDays,
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
                            dynamicDiscountToken = null,
                            promoBonusDays = 0,
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
        val market = state.detectedMarket

        viewModelScope.launch {
            if (market == AppMarket.CAFE_BAZAAR || market == AppMarket.MYKET) {
                _uiState.update {
                    it.copy(
                        isPurchasing = true,
                        purchaseProgressMessage = "در حال انتقال به ${market.titleFa}...",
                        errorMessage = null
                    )
                }

                val purchaseResult = billingGateway.purchaseProduct(
                    sku = plan.id,
                    payload = "user_purchase",
                    dynamicPriceToken = state.dynamicDiscountToken
                )

                when (purchaseResult) {
                    is MarketPurchaseResult.Success -> {
                        _uiState.update {
                            it.copy(purchaseProgressMessage = "در حال تایید پرداخت توسط سرور...")
                        }

                        val verifyResult = verifyMarketPurchaseUseCase(
                            market = market.id,
                            planId = plan.id,
                            sku = purchaseResult.sku,
                            purchaseToken = purchaseResult.purchaseToken,
                            orderId = purchaseResult.orderId,
                            developerPayload = purchaseResult.developerPayload,
                            promoCode = if (state.appliedDiscountPercent > 0) state.promoCodeInput.trim() else null
                        )

                        when (verifyResult) {
                            is DataResult.Success -> {
                                billingGateway.consumePurchase(purchaseResult.purchaseToken)
                                _uiState.update {
                                    it.copy(
                                        isPurchasing = false,
                                        purchaseProgressMessage = null,
                                        currentStatus = verifyResult.data
                                    )
                                }
                                _effect.send(
                                    SubscriptionEffect.SubscriptionActivated(
                                        remainingDays = verifyResult.data.remainingDays,
                                        planTitle = plan.titleFa
                                    )
                                )
                            }
                            is DataResult.Failure -> {
                                val errorMsg = verifyResult.appError.getErrorMessage().ifBlank {
                                    "خطا در تایید خرید توسط سرور. لطفاً با پشتیبانی تماس بگیرید."
                                }
                                _uiState.update {
                                    it.copy(
                                        isPurchasing = false,
                                        purchaseProgressMessage = null,
                                        errorMessage = errorMsg
                                    )
                                }
                                _effect.send(SubscriptionEffect.ShowSnackbar(errorMsg))
                            }
                        }
                    }
                    is MarketPurchaseResult.UserCancelled -> {
                        _uiState.update {
                            it.copy(isPurchasing = false, purchaseProgressMessage = null)
                        }
                    }
                    is MarketPurchaseResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseProgressMessage = null,
                                errorMessage = purchaseResult.message
                            )
                        }
                        _effect.send(SubscriptionEffect.ShowSnackbar(purchaseResult.message))
                    }
                }
            } else {
                // Direct Web or Sideload fallback
                _uiState.update {
                    it.copy(
                        isPurchasing = true,
                        purchaseProgressMessage = "در حال ایجاد شناسه پرداخت مستقیم...",
                        errorMessage = null
                    )
                }

                val result = createDirectCheckoutUseCase(
                    planId = plan.id,
                    promoCode = if (state.appliedDiscountPercent > 0) state.promoCodeInput.trim() else null
                )

                when (result) {
                    is DataResult.Success -> {
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseProgressMessage = null
                            )
                        }
                        _effect.send(SubscriptionEffect.OpenWebUrl(result.data.paymentUrl))
                    }
                    is DataResult.Failure -> {
                        val errorMsg = result.appError.getErrorMessage().ifBlank {
                            "خطا در برقراری ارتباط با درگاه پرداخت مستقیم"
                        }
                        _uiState.update {
                            it.copy(
                                isPurchasing = false,
                                purchaseProgressMessage = null,
                                errorMessage = errorMsg
                            )
                        }
                        _effect.send(SubscriptionEffect.ShowSnackbar(errorMsg))
                    }
                }
            }
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
