package ir.aispeaking.network.dto.plan

import kotlinx.serialization.Serializable

@Serializable
data class PlanResponse(
    val id: String,
    val title: String,
    val description: String,
    val name: String,
    val price: String,
    val cafeBazaarId: String,
    val discountedPrice: String?,
    val dayDuration: Int,
    val appliedDiscount: AppliedDiscountResponse? = null,
    val createdAt: String
)

@Serializable
data class AppliedDiscountResponse(
    val bazaarToken: String,
    val id: String,
)
