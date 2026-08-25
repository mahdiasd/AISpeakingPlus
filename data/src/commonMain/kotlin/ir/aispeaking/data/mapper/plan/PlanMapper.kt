package ir.aispeaking.data.mapper.plan

import ir.aispeaking.domain.model.plan.AppliedDiscount
import ir.aispeaking.domain.model.plan.Plan
import ir.aispeaking.network.dto.plan.AppliedDiscountResponse
import ir.aispeaking.network.dto.plan.PlanResponse
import ir.aispeaking.utils.time.toInstant

fun PlanResponse.toDomain(): Plan {
    return Plan(
        id = id,
        title = title,
        name = name,
        description = description,
        price = price,
        discountedPrice = discountedPrice,
        dayDuration = dayDuration,
        appliedDiscount = appliedDiscount?.toDomain(),
        createdAt = createdAt.toInstant(),
        cafeBazaarId = cafeBazaarId
    )
}

fun AppliedDiscountResponse.toDomain(): AppliedDiscount {
    return AppliedDiscount(
        bazaarDiscountToken = bazaarToken,
        id = id
    )
}