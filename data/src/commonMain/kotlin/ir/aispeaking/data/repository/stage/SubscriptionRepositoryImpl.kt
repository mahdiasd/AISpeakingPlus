package ir.aispeaking.data.repository.stage

import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.domain.repository.stage.SubscriptionRepository
import ir.aispeaking.network.api.stage.SubscriptionApi
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
}
