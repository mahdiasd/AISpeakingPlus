package ir.aispeaking.domain.repository.purchase

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.paging.Paging
import ir.aispeaking.domain.model.purchase.Purchase
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun createPurchase(purchase: Purchase): Flow<DataResult<Purchase>>
    suspend fun getPurchasesByUserId(): Flow<DataResult<Paging<Purchase>>>

    suspend fun getActivePurchasesByUserId(): Flow<DataResult<List<Purchase>>>
}