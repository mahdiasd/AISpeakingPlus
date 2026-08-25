package ir.speaking.feature.plan.dto

import ir.speaking.core.utils.toUUIDOrNull
import ir.speaking.feature.plan.model.Plan
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class PlanRequest(
    val id: String? = null,
    val title: String,
    val name: String,
    val description: String,
    val price: String,
    val cafeBazaarId: String,
    val discountedPrice: String?,
    val visibility: Boolean? = true,
    val dayDuration: Int
)

fun PlanRequest.toPlan() = Plan(
    id = this.id?.toUUIDOrNull() ?: UUID.randomUUID(),
    title = this.title,
    name = this.name,
    description = this.description,
    price = this.price,
    cafeBazaarId = this.cafeBazaarId,
    discountedPrice = this.discountedPrice,
    dayDuration = this.dayDuration,
    visibility = this.visibility ?: true,
    createdAt = Clock.System.now()
)