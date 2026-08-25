package ir.aispeaking.data.repository.purchase

import ir.aispeaking.data.mapper.paginate.toPaging
import ir.aispeaking.data.mapper.purchase.toDomain
import ir.aispeaking.data.mapper.purchase.toRequest
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.DeviceError
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.repository.purchase.PurchaseRepository
import ir.aispeaking.network.api.purchase.PurchaseApiService

import ir.aispeaking.utils.constant.AppConstant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class PurchaseRepositoryImpl(
    private val apiService: PurchaseApiService,
) : PurchaseRepository {

    override suspend fun createPurchase(purchase: Purchase): Flow<DataResult<Purchase>> = flow {
        when (val result = safeCall { apiService.createPurchase(purchase.toRequest()) }) {
            is DataResult.Success -> {
                AppConstant.needToRefreshActivePurchase = true
                emit(DataResult.Success(result.data.toDomain()))
            }
            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getPurchasesByUserId(): Flow<DataResult<Paging<Purchase>>> = flow {
        when (val result = safeCall { apiService.getPurchasesByUserId() }) {
            is DataResult.Success -> {
                result.pagingMeta?.let { pagingMeta ->
                    emit(DataResult.Success(pagingMeta.toPaging(result.data.map { it.toDomain() })))
                } ?: emit(DataResult.Failure(DeviceError.MetadataNotFound))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }

    override suspend fun getActivePurchasesByUserId(): Flow<DataResult<List<Purchase>>> = flow {
        when (val result = safeCall { apiService.getActiveUserPurchases() }) {
            is DataResult.Success -> {
                AppConstant.needToRefreshActivePurchase = false
                emit(DataResult.Success(result.data.map { it.toDomain() }))
            }

            is DataResult.Failure -> emit(DataResult.Failure(result.appError))
        }
    }
}