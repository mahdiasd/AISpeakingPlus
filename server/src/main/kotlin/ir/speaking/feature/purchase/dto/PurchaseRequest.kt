package ir.speaking.feature.purchase.dto

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseRequest(
    val id: String? = null,
    val userId: String,
    val planId: String,
    val discountCodeId: String?,
    val amountPaid: String,
    val status: String,
    val token: String,
)
