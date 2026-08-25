package ir.aispeaking.network.api.discount

import ir.aispeaking.network.dto.plan.PlanResponse
import ir.aispeaking.network.model.NetworkResponse

interface DiscountCodeApiService {
    suspend fun checkDiscount(code: String): NetworkResponse<List<PlanResponse>>
}
