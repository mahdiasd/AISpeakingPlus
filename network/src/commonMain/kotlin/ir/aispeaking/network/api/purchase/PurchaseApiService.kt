package ir.aispeaking.network.api.purchase

import ir.aispeaking.network.dto.purchase.PurchaseRequest
import ir.aispeaking.network.dto.purchase.PurchaseResponse
import ir.aispeaking.network.model.NetworkResponse

interface PurchaseApiService {
    suspend fun createPurchase(request: PurchaseRequest): NetworkResponse<PurchaseResponse>
    suspend fun getPurchasesByUserId(): NetworkResponse<List<PurchaseResponse>>
    suspend fun getActiveUserPurchases(): NetworkResponse<List<PurchaseResponse>>
}