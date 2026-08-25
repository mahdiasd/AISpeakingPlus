package ir.speaking.feature.discount.dto.response

import ir.speaking.feature.discount.model.DiscountCode
import ir.speaking.feature.discount.model.DiscountPlanToken
import kotlinx.serialization.Serializable

@Serializable
data class DiscountCodeResponse(
    val id: String,
    val code: String,
    val percentage: Int,
    val isActive: Boolean,
    val expiryDate: String,
    val createdAt: String,
    val tokens: List<DiscountPlanToken>,
    val applicablePlans: List<String>
)

fun DiscountCode.toResponse(tokens: List<DiscountPlanToken> = listOf()) = DiscountCodeResponse(
    id = id.toString(),
    code = code,
    percentage = percentage,
    isActive = isActive,
    expiryDate = expiryDate.toString(),
    createdAt = createdAt.toString(),
    tokens = tokens,
    applicablePlans = applicablePlans.map { it.toString() },
)