package ir.aispeaking.domain.repository.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.domain.model.stage.SubscriptionStatus

interface SubscriptionRepository {
    suspend fun getSubscriptionPlans(): DataResult<List<SubscriptionPlan>>
    suspend fun getSubscriptionStatus(): DataResult<SubscriptionStatus>
}
