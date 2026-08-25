package ir.speaking.core.utils

import ir.speaking.core.exeptions.AppException
import java.util.*

object AppUtils {
    fun validUUID(stringId: String?): UUID? {
        return try {
            UUID.fromString(stringId)
        } catch (exception: IllegalArgumentException) {
            throw AppException.BadRequest("Invalid UUID!")
        }
    }


    fun isValidMobileNumber(mobile: String): Boolean {
        return mobile.length == 11 && mobile.all { it.isDigit() }
    }


    /**
     * Calculates the final price after applying a discount.
     *
     * @param originalPrice The initial price of the item. Must be a non-negative number.
     * @param discountPercent The discount percentage to apply. Must be between 0.0 and 100.0.
     * @return The final price after the discount is applied.
     * @throws IllegalArgumentException if the price or discount percentage is invalid.
     */
    fun calculateDiscountPrice(originalPrice: Long, discountPercent: Long): Long {
        require(originalPrice >= 0) { "Original price cannot be negative." }
        require(discountPercent in 0..100) { "Discount percent must be between 0 and 100." }

        val discountAmount = originalPrice * (discountPercent.toFloat() / 100.toFloat())
        return (originalPrice - discountAmount).toLong()
    }
}