package ir.aispeaking.domain.model.purchase

import ir.aispeaking.domain.model.plan.Plan
import kotlin.time.Instant

data class Purchase(
    val id: String?,
    val userId: String,
    val plan: Plan,
    val discountCodeId: String?,
    val purchaseDate: Instant,
    val expiryDate: Instant?,
    val amountPaid: String,
    val status: PurchaseStatus?,
    val token: String?,
    val createdAt: Instant
)

enum class PurchaseStatus { SUCCESS, FAILED }
