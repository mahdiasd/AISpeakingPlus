package ir.aispeaking.domain.repository.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.DirectCheckoutResult
import ir.aispeaking.domain.model.stage.PromoValidationResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus

interface SubscriptionRepository {
    suspend fun getSubscriptionPlans(): DataResult<List<SubscriptionPlan>>
    suspend fun getSubscriptionStatus(): DataResult<SubscriptionStatus>
    suspend fun subscribe(planId: String, promoCode: String? = null): DataResult<SubscriptionStatus>
    suspend fun validatePromo(promoCode: String): DataResult<PromoValidationResult>
    suspend fun validateMarketPromo(promoCode: String, planId: String, market: String): DataResult<PromoValidationResult>
    suspend fun verifyMarketPurchase(
        market: String,
        planId: String,
        sku: String,
        purchaseToken: String,
        orderId: String? = null,
        developerPayload: String? = null,
        promoCode: String? = null
    ): DataResult<SubscriptionStatus>
    suspend fun createDirectCheckout(planId: String, promoCode: String? = null): DataResult<DirectCheckoutResult>
}
