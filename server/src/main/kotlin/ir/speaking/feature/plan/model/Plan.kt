package ir.speaking.feature.plan.model

import kotlinx.datetime.Instant
import java.util.*

data class Plan(
    val id: UUID,
    val title: String,
    val name: String,
    val description: String,
    val price: String,
    val cafeBazaarId: String,
    val discountedPrice: String?,
    val dayDuration: Int,
    val visibility: Boolean,
    val createdAt: Instant
)