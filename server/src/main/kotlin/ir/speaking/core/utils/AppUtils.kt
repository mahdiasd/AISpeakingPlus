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


    /**
     * Converts Persian and Arabic digits in a string to standard ASCII English digits (0-9).
     */
    fun normalizeDigits(input: String): String {
        return input.map { char ->
            when (char) {
                in '۰'..'۹' -> '0' + (char - '۰')
                in '٠'..'٩' -> '0' + (char - '٠')
                else -> char
            }
        }.joinToString("")
    }

    /**
     * Normalizes Iranian mobile numbers to the standard 11-digit format starting with 09 (e.g. "09152413498").
     * Supports:
     * - "09152413498" -> "09152413498"
     * - "9152413498" -> "09152413498"
     * - "+989152413498" -> "09152413498"
     * - "00989152413498" -> "09152413498"
     * - "989152413498" -> "09152413498"
     * - Persian/Arabic numerals (e.g. "۰۹۱۵۲۴۱۳۴۹۸")
     * - Formatting characters (spaces, dashes, parentheses)
     */
    fun normalizeMobileNumber(rawMobile: String?): String? {
        if (rawMobile.isNullOrBlank()) return null
        val clean = normalizeDigits(rawMobile.trim())
            .replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")

        return when {
            clean.startsWith("+98") && clean.length == 13 -> "0" + clean.removePrefix("+98")
            clean.startsWith("0098") && clean.length == 14 -> "0" + clean.removePrefix("0098")
            clean.startsWith("98") && clean.length == 12 -> "0" + clean.removePrefix("98")
            clean.startsWith("9") && clean.length == 10 -> "0$clean"
            clean.startsWith("09") && clean.length == 11 -> clean
            else -> clean
        }
    }

    fun isValidMobileNumber(mobile: String): Boolean {
        val normalized = normalizeMobileNumber(mobile) ?: return false
        return normalized.length == 11 && normalized.startsWith("09") && normalized.all { it.isDigit() }
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