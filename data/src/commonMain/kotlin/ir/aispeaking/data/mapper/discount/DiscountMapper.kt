package ir.aispeaking.data.mapper.discount

import ir.aispeaking.domain.model.discount.DiscountCode
import ir.aispeaking.network.dto.discount.DiscountCodeResponse
import ir.aispeaking.utils.time.toInstant

fun DiscountCodeResponse.toDomain(): DiscountCode {
    return DiscountCode(
        id = id,
        code = code,
        percentage = percentage,
        isActive = isActive,
        expiryDate = expiryDate.toInstant(),
        createdAt = createdAt.toInstant(),
        dynamicPriceToken = dynamicPriceToken,
        applicablePlans = applicablePlans
    )
}