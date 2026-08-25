package ir.aispeaking.network.dto.purchase

import ir.aispeaking.network.dto.plan.PlanResponse
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
