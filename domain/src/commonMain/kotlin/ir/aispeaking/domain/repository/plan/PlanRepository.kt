package ir.aispeaking.domain.repository.plan

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.plan.Plan
import kotlinx.coroutines.flow.Flow

interface PlanRepository {
    suspend fun getAllPlans(): Flow<DataResult<List<Plan>>>
}