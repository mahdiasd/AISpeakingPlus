package ir.aispeaking.network.dto.discount

import kotlinx.serialization.Serializable

@Serializable
data class DiscountCodeResponse(
    val id: String,
    val code: String,
    val percentage: Int,
    val isActive: Boolean,
    val expiryDate: String,
    val createdAt: String,
    val dynamicPriceToken: String,
    val applicablePlans: List<String>
)