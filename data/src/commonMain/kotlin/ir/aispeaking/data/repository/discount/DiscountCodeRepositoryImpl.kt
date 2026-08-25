package ir.aispeaking.data.repository.discount

import ir.aispeaking.data.mapper.plan.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.plan.Plan
import ir.aispeaking.domain.repository.discount.DiscountCodeRepository
import ir.aispeaking.network.api.discount.DiscountCodeApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class DiscountCodeRepositoryImpl(
    private val apiService: DiscountCodeApiService,
) : DiscountCodeRepository {

    override suspend fun checkDiscount(code: String): Flow<DataResult<List<Plan>>> =
        flow {
            when (val result = safeCall { apiService.checkDiscount(code) }) {
                is DataResult.Success -> emit(DataResult.Success(result.data.map { it.toDomain() }))
                is DataResult.Failure -> emit(DataResult.Failure(result.appError))
            }
        }
}