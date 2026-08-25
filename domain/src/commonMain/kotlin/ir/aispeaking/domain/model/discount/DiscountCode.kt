package ir.aispeaking.domain.model.discount

import kotlin.time.Instant

data class DiscountCode(
    val id: String,
    val code: String,
    val dynamicPriceToken: String, // Server should be generate token for discount.
    val percentage: Int,
    val isActive: Boolean,
    val expiryDate: Instant,
    val createdAt: Instant,
    val applicablePlans: List<String>
)