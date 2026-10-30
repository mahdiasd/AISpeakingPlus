package ir.aispeaking.subscription

import ir.aispeaking.domain.model.payment.AppMarket
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String = "plan-3m",
    val currentStatus: SubscriptionStatus? = null,
    val detectedMarket: AppMarket = AppMarket.DIRECT_WEB,

    // Promo code state
    val promoCodeInput: String = "",
    val appliedDiscountPercent: Int = 0,
    val dynamicDiscountToken: String? = null,
    val promoBonusDays: Int = 0,
    val promoFeedbackMessage: String? = null,
    val isPromoError: Boolean = false,

    // Purchasing state
    val isPurchasing: Boolean = false,
    val purchaseProgressMessage: String? = null,
    val errorMessage: String? = null,

    // Auth / Register bottom sheet state
    val showRegisterDialog: Boolean = false,
    val isRegisterLoading: Boolean = false,
    val registerStep: Int = 1,
    val registerError: String? = null
) {
    val selectedPlan: SubscriptionPlan?
        get() = plans.find { it.id == selectedPlanId } ?: plans.firstOrNull()

    fun calculateFinalPrice(plan: SubscriptionPlan): Long {
        if (detectedMarket == AppMarket.MYKET) return plan.priceTomans
        if (appliedDiscountPercent <= 0) return plan.priceTomans
        val factor = (100 - appliedDiscountPercent).coerceAtLeast(0)
        return (plan.priceTomans * factor) / 100
    }
}

sealed interface SubscriptionIntent {
    data object LoadData : SubscriptionIntent
    data class SelectPlan(val planId: String) : SubscriptionIntent
    data class OnPromoCodeChanged(val code: String) : SubscriptionIntent
    data object ApplyPromoCode : SubscriptionIntent
    data object PurchaseSelectedPlan : SubscriptionIntent
    data object DismissError : SubscriptionIntent
    data class SendRegisterOtp(val phone: String) : SubscriptionIntent
    data class VerifyRegisterOtp(val phone: String, val otp: String) : SubscriptionIntent
    data object ResetRegisterStep : SubscriptionIntent
    data object DismissRegisterDialog : SubscriptionIntent
}

sealed interface SubscriptionEffect {
    data object NavigateBack : SubscriptionEffect
    data class SubscriptionActivated(val remainingDays: Int, val planTitle: String) : SubscriptionEffect
    data class ShowSnackbar(val message: String) : SubscriptionEffect
    data class OpenWebUrl(val url: String) : SubscriptionEffect
}
