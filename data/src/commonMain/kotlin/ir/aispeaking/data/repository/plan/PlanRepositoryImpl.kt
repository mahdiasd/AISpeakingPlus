package ir.aispeaking.data.repository.plan
import ir.aispeaking.data.mapper.plan.toDomain
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.plan.Plan
import ir.aispeaking.domain.repository.plan.PlanRepository
import ir.aispeaking.network.api.plan.PlanApiService

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class PlanRepositoryImpl(
    private val apiService: PlanApiService,
) : PlanRepository {
    override suspend fun getAllPlans(): Flow<DataResult<List<Plan>>> = flow {
        when (val result = safeCall { apiService.getAllPlans() }) {
            is DataResult.Success -> emit(DataResult.Success(result.data.map { it.toDomain() }))
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }
}