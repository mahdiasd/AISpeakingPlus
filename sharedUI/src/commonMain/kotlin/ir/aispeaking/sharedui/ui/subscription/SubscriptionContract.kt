package ir.aispeaking.sharedui.ui.subscription

import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlanId: String = "plan-3m",
    val currentStatus: SubscriptionStatus? = null,
    val promoCodeInput: String = "",
    val appliedDiscountPercent: Int = 0,
    val promoFeedbackMessage: String? = null,
    val isPromoError: Boolean = false,
    val isPurchasing: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedPlan: SubscriptionPlan?
        get() = plans.find { it.id == selectedPlanId } ?: plans.firstOrNull()

    fun calculateFinalPrice(plan: SubscriptionPlan): Long {
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
}

sealed interface SubscriptionEffect {
    data object NavigateBack : SubscriptionEffect
    data class SubscriptionActivated(val remainingDays: Int, val planTitle: String) : SubscriptionEffect
    data class ShowSnackbar(val message: String) : SubscriptionEffect
}
