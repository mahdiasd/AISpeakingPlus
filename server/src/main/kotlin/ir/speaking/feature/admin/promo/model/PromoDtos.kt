package ir.speaking.feature.admin.promo.model

import kotlinx.serialization.Serializable

@Serializable
data class AdminPromoCodeItemDto(
    val id: String,
    val code: String,
    val title: String? = null,
    val discountType: String, // "PERCENTAGE" or "FIXED_AMOUNT"
    val discountValue: Long,
    val maxDiscountCap: Long? = null,
    val minOrderAmount: Long? = null,
    val applicablePlans: List<String> = emptyList(), // empty = ALL
    val maxUses: Int? = null,
    val usedCount: Int = 0,
    val isOncePerUser: Boolean = true,
    val startsAt: String? = null,
    val expiresAt: String? = null,
    val isActive: Boolean = true,
    val isExpired: Boolean = false,
    val createdAt: String
)

@Serializable
data class AdminPromoCodeUpsertRequest(
    val code: String,
    val title: String? = null,
    val discountType: String, // "PERCENTAGE" or "FIXED_AMOUNT"
    val discountValue: Long,
    val maxDiscountCap: Long? = null,
    val minOrderAmount: Long? = null,
    val applicablePlans: List<String> = emptyList(),
    val maxUses: Int? = null,
    val isOncePerUser: Boolean = true,
    val startsAt: String? = null,
    val expiresAt: String? = null,
    val isActive: Boolean = true
)

@Serializable
data class AdminPromoStatusToggleRequest(
    val isActive: Boolean
)

@Serializable
data class AdminPromoRedemptionItemDto(
    val id: String,
    val userId: String,
    val phoneNumber: String,
    val nickName: String? = null,
    val planId: String,
    val originalPrice: Long,
    val discountAmount: Long,
    val finalPrice: Long,
    val redeemedAt: String
)

@Serializable
data class AdminPromoStatsDto(
    val totalCodes: Int,
    val activeCodes: Int,
    val totalRedemptions: Int,
    val totalDiscountGiven: Long
)

@Serializable
data class AdminPromoListResponse(
    val items: List<AdminPromoCodeItemDto>,
    val stats: AdminPromoStatsDto
)
