package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.PromoValidationResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.domain.repository.stage.SubscriptionRepository
import org.koin.core.annotation.Factory

@Factory
class GetSubscriptionPlansUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): DataResult<List<SubscriptionPlan>> = repository.getSubscriptionPlans()
}

@Factory
class CheckSubscriptionStatusUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(): DataResult<SubscriptionStatus> = repository.getSubscriptionStatus()
}

@Factory
class SubscribePlanUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(planId: String, promoCode: String? = null): DataResult<SubscriptionStatus> =
        repository.subscribe(planId = planId, promoCode = promoCode)
}

@Factory
class ValidatePromoCodeUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(promoCode: String): DataResult<PromoValidationResult> =
        repository.validatePromo(promoCode = promoCode)
}

@Factory
class VerifyMarketPurchaseUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(
        market: String,
        planId: String,
        sku: String,
        purchaseToken: String,
        orderId: String? = null,
        developerPayload: String? = null,
        promoCode: String? = null
    ): DataResult<SubscriptionStatus> = repository.verifyMarketPurchase(
        market = market,
        planId = planId,
        sku = sku,
        purchaseToken = purchaseToken,
        orderId = orderId,
        developerPayload = developerPayload,
        promoCode = promoCode
    )
}

@Factory
class ValidateMarketPromoUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(
        promoCode: String,
        planId: String,
        market: String
    ): DataResult<PromoValidationResult> = repository.validateMarketPromo(
        promoCode = promoCode,
        planId = planId,
        market = market
    )
}

@Factory
class CreateDirectCheckoutUseCase(
    private val repository: SubscriptionRepository
) {
    suspend operator fun invoke(
        planId: String,
        promoCode: String? = null
    ): DataResult<ir.aispeaking.domain.model.stage.DirectCheckoutResult> = repository.createDirectCheckout(
        planId = planId,
        promoCode = promoCode
    )
}
