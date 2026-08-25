package ir.speaking.feature.purchase.dto

import ir.speaking.feature.plan.dto.PlanResponse
import ir.speaking.feature.plan.dto.toResponse
import ir.speaking.feature.plan.model.Plan
import ir.speaking.feature.purchase.model.Purchase
import kotlinx.serialization.Serializable

@Serializable
data class PurchaseResponse(
    val id: String,
    val userId: String,
    val plan: PlanResponse,
    val discountCodeId: String?,
    val purchaseDate: String,
    val expiryDate: String,
    val amountPaid: String,
    val status: String,
    val createdAt: String
)

fun Purchase.toResponse(plan: Plan): PurchaseResponse {
    return PurchaseResponse(
        id = id.toString(),
        userId = userId.toString(),
        plan = plan.toResponse(),
        discountCodeId = discountCodeId.toString(),
        purchaseDate = purchaseDate.toString(),
        expiryDate = expiryDate.toString(),
        amountPaid = amountPaid,
        status = status.name,
        createdAt = createdAt.toString()
    )
}