package ir.aispeaking.data.repository.stage

import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.PromoValidationResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.domain.repository.stage.SubscriptionRepository
import ir.aispeaking.network.api.stage.SubscriptionApi
import ir.aispeaking.network.model.stage.dto.SubscribeRequestDto
import ir.aispeaking.network.model.stage.dto.ValidatePromoRequestDto
import org.koin.core.annotation.Single

@Single
class SubscriptionRepositoryImpl(
    private val subscriptionApi: SubscriptionApi
) : SubscriptionRepository {

    override suspend fun getSubscriptionPlans(): DataResult<List<SubscriptionPlan>> {
        val result = safeCall { subscriptionApi.getPlans() }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        val plans = result.data.map {
            SubscriptionPlan(
                id = it.id,
                type = it.type,
                titleFa = it.titleFa,
                durationDays = it.durationDays,
                priceTomans = it.priceTomans,
                discountPercent = it.discountPercent,
                badge = it.badge
            )
        }
        return DataResult.Success(plans)
    }

    override suspend fun getSubscriptionStatus(): DataResult<SubscriptionStatus> {
        val result = safeCall { subscriptionApi.getStatus() }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        val status = SubscriptionStatus(
            isSubscriber = result.data.isSubscriber,
            planType = result.data.planType,
            expiresAt = result.data.expiresAt,
            remainingDays = result.data.remainingDays
        )
        return DataResult.Success(status)
    }

    override suspend fun subscribe(planId: String, promoCode: String?): DataResult<SubscriptionStatus> {
        val result = safeCall {
            subscriptionApi.subscribe(
                SubscribeRequestDto(
                    planId = planId,
                    promoCode = promoCode
                )
            )
        }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        val status = SubscriptionStatus(
            isSubscriber = result.data.isSubscriber,
            planType = result.data.planType,
            expiresAt = result.data.expiresAt,
            remainingDays = result.data.remainingDays
        )
        return DataResult.Success(status)
    }

    override suspend fun validatePromo(promoCode: String): DataResult<PromoValidationResult> {
        return validateMarketPromo(promoCode = promoCode, planId = "plan-3m", market = "DIRECT_WEB")
    }

    override suspend fun validateMarketPromo(
        promoCode: String,
        planId: String,
        market: String
    ): DataResult<PromoValidationResult> {
        val result = safeCall {
            subscriptionApi.validatePromo(
                ValidatePromoRequestDto(
                    promoCode = promoCode,
                    planId = planId,
                    market = market
                )
            )
        }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        return DataResult.Success(
            PromoValidationResult(
                promoCode = result.data.promoCode,
                discountPercent = result.data.discountPercent,
                message = result.data.userMessage ?: result.data.message,
                dynamicDiscountToken = result.data.dynamicDiscountToken,
                bonusDays = result.data.bonusDays,
                originalPriceTomans = result.data.originalPriceTomans,
                finalPriceTomans = result.data.finalPriceTomans
            )
        )
    }

    override suspend fun verifyMarketPurchase(
        market: String,
        planId: String,
        sku: String,
        purchaseToken: String,
        orderId: String?,
        developerPayload: String?,
        promoCode: String?
    ): DataResult<SubscriptionStatus> {
        val result = safeCall {
            subscriptionApi.verifyMarketPurchase(
                ir.aispeaking.network.model.stage.dto.VerifyMarketPurchaseRequestDto(
                    market = market,
                    planId = planId,
                    sku = sku,
                    purchaseToken = purchaseToken,
                    orderId = orderId,
                    developerPayload = developerPayload,
                    promoCode = promoCode
                )
            )
        }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        return DataResult.Success(
            SubscriptionStatus(
                isSubscriber = result.data.isSubscriber,
                planType = result.data.planType,
                expiresAt = result.data.expiresAt,
                remainingDays = result.data.remainingDays
            )
        )
    }

    override suspend fun createDirectCheckout(
        planId: String,
        promoCode: String?
    ): DataResult<ir.aispeaking.domain.model.stage.DirectCheckoutResult> {
        val result = safeCall {
            subscriptionApi.directCheckout(
                ir.aispeaking.network.model.stage.dto.DirectCheckoutRequestDto(
                    planId = planId,
                    promoCode = promoCode
                )
            )
        }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        return DataResult.Success(
            ir.aispeaking.domain.model.stage.DirectCheckoutResult(
                paymentUrl = result.data.paymentUrl,
                transactionId = result.data.transactionId,
                amountTomans = result.data.amountTomans
            )
        )
    }
}

