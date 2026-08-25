package ir.aispeaking.domain.model.plan

import ir.aispeaking.utils.safeToInt
import kotlin.time.Instant

data class Plan(
    val id: String,
    val title: String,
    val name: String,
    val description: String,
    val price: String,
    val cafeBazaarId: String,
    val appliedDiscount: AppliedDiscount? = null,
    val discountedPrice: String?,
    val dayDuration: Int, val createdAt: Instant
) {
    fun intPrice(): Int {
        return price.replace(",", "").replace(" ", "").safeToInt()
    }

    private fun intDiscountedPrice(): Int? {
        if (discountedPrice == null) return null
        return discountedPrice.replace(",", "").replace(" ", "").safeToInt()
    }

    fun discountPercent(): Int {
        val originalPrice = intPrice()
        val currentDiscountedPrice = intDiscountedPrice()

        if (originalPrice <= 0 || currentDiscountedPrice == null || currentDiscountedPrice >= originalPrice) {
            return 0
        }

        val discountAmount = originalPrice - currentDiscountedPrice

        return (discountAmount * 100) / originalPrice
    }
}