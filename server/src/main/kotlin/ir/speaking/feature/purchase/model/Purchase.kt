package ir.speaking.feature.purchase.model

import kotlinx.datetime.Instant
import java.util.*

enum class PurchaseStatus { SUCCESS, FAILED }

data class Purchase(
    val id: UUID,
    val userId: UUID,
    val planId: UUID,
    val discountCodeId: UUID?,
    val purchaseDate: Instant,
    val expiryDate: Instant?,
    val amountPaid: String,
    val token: String, // token after success purchase from cafebazaar
    val status: PurchaseStatus,
    val createdAt: Instant
)

fun String.toPurchaseStatus(): PurchaseStatus {
    return when (this.lowercase()) {
        "success" -> PurchaseStatus.SUCCESS
        "failed" -> PurchaseStatus.FAILED
        else -> {
            throw Exception("Unknown PurchaseStatus: $this")
        }
    }
}