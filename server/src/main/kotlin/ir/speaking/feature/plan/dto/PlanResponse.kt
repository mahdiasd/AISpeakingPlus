package ir.speaking.feature.plan.dto

import ir.speaking.feature.plan.model.Plan
import kotlinx.serialization.Serializable

@Serializable
data class PlanResponse(
    val id: String,
    val title: String,
    val name: String,
    val description: String,
    val price: String,
    val cafeBazaarId: String,
    val discountedPrice: String?,
    val dayDuration: Int,
    val appliedDiscount: AppliedDiscount? = null,
    val createdAt: String
)

@Serializable
data class AppliedDiscount(
    val bazaarToken: String,
    val id: String,
)


fun Plan.toResponse(
    dynamicPriceToken: String? = null,
    discountId: String? = null,
): PlanResponse {
    return PlanResponse(
        id = id.toString(),
        title = title,
        name = name,
        appliedDiscount = if (dynamicPriceToken.isNullOrEmpty() || discountId.isNullOrEmpty()) null
        else AppliedDiscount(
            bazaarToken = dynamicPriceToken,
            id = discountId
        ),
        description = description,
        price = price,
        cafeBazaarId = cafeBazaarId,
        discountedPrice = discountedPrice,
        dayDuration = dayDuration,
        createdAt = createdAt.toString()
    )
}