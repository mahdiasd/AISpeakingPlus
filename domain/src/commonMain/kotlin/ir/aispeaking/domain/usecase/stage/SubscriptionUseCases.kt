package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
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
