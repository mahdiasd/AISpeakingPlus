package ir.speaking.feature.discount.model

import kotlinx.datetime.LocalDateTime
import java.util.*

data class DiscountCode(
    val id: UUID,
    val code: String,
    val percentage: Int,
    val isActive: Boolean,
    val expiryDate: LocalDateTime,
    val createdAt: LocalDateTime,
    val applicablePlans: List<UUID>
)