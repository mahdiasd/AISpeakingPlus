package ir.speaking.feature.user.dto.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
    val giftPurchase: Int = 0
)