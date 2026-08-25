package ir.speaking.feature.discount.model

import kotlinx.serialization.Serializable

@Serializable
data class DiscountPlanToken(
    val dynamicPriceToken: String,
    val cafeBazaarPlanId: String
)

