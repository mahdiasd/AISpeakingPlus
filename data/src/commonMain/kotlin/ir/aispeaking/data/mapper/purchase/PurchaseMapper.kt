package ir.aispeaking.data.mapper.purchase

import ir.aispeaking.data.mapper.plan.toDomain
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.model.purchase.PurchaseStatus
import ir.aispeaking.network.dto.purchase.PurchaseRequest
import ir.aispeaking.network.dto.purchase.PurchaseResponse
import ir.aispeaking.utils.time.toInstant

fun PurchaseResponse.toDomain(): Purchase {
    return Purchase(
        id = id,
        userId = userId,
        plan = plan.toDomain(),
        discountCodeId = discountCodeId,
        purchaseDate = purchaseDate.toInstant(),
        expiryDate = expiryDate.toInstant(),
        amountPaid = amountPaid,
        status = status.toPurchaseStatus(),
        token = null,
        createdAt = createdAt.toInstant()
    )
}

fun Purchase.toRequest(): PurchaseRequest {
    return PurchaseRequest(
        id = id,
        userId = userId,
        planId = plan.id,
        discountCodeId = discountCodeId,
        amountPaid = amountPaid,
        status = status.toString(),
        token = token ?: "",
    )
}

fun String.toPurchaseStatus(): PurchaseStatus {
    return when (this.lowercase()) {
        "success" -> PurchaseStatus.SUCCESS
        "failed" -> PurchaseStatus.FAILED
        else -> {
            throw Exception("Unknown PurchaseStatus: $this")
        }
    }
}