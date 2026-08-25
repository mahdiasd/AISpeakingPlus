package ir.speaking.feature.discount.dto.request

import ir.speaking.core.utils.now
import ir.speaking.core.utils.toUUID
import ir.speaking.core.utils.toUUIDOrNull
import ir.speaking.feature.discount.model.DiscountCode
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class DiscountCodeRequest(
    val id: String? = null,
    val code: String,
    val percentage: Int,
    val isActive: Boolean = true,
    val activeDay: Int = 5,
    val applicablePlans: List<String>? = null,
)

fun DiscountCodeRequest.toDiscountCode(): DiscountCode = DiscountCode(
    id = id?.toUUIDOrNull() ?: UUID.randomUUID(),
    code = code,
    percentage = percentage,
    isActive = isActive,
    expiryDate = java.time.LocalDateTime.now().plusDays(activeDay.toLong()).toKotlinLocalDateTime(),
    createdAt = LocalDateTime.now(),
    applicablePlans = applicablePlans?.map { it.toUUID() } ?: emptyList(),
)