package ir.aispeaking.network.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserResponse,
    val giftPurchase: Int? = 0,
)