package ir.aispeaking.domain.repository.discount

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.discount.DiscountCode
import ir.aispeaking.domain.model.plan.Plan
import kotlinx.coroutines.flow.Flow

interface DiscountCodeRepository {
    suspend fun checkDiscount(code: String): Flow<DataResult<List<Plan>>>
}
