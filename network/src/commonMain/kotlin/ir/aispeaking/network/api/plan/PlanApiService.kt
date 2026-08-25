package ir.aispeaking.network.api.plan

import ir.aispeaking.network.dto.plan.PlanResponse
import ir.aispeaking.network.model.NetworkResponse

interface PlanApiService {
    suspend fun getAllPlans(): NetworkResponse<List<PlanResponse>>
}