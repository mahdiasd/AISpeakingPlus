package ir.speaking.feature.purchase.repository

import ir.speaking.core.response.PagedList
import ir.speaking.feature.plan.model.Plan
import ir.speaking.feature.purchase.dto.PurchaseRequest
import ir.speaking.feature.purchase.dto.PurchaseResponse
import java.util.*

interface PurchaseRepository {
    suspend fun createPurchase(purchase: PurchaseRequest): PurchaseResponse

    suspend fun giftCharge(userId: UUID, plan: Plan): PurchaseResponse

    suspend fun deletePurchase(id: UUID): Boolean

    suspend fun getActivePurchasesByUserId(userId: UUID): List<PurchaseResponse>

    suspend fun getPurchasesByUserId(userId: UUID, page: Int, pageSize: Int): PagedList<PurchaseResponse>
}