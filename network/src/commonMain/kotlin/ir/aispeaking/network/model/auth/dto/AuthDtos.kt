package ir.aispeaking.network.model.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class SendOtpRequestDto(
    val mobile: String
)

@Serializable
data class SendOtpResponseDto(
    val mobile: String,
    val expiresInSeconds: Int = 120
)

@Serializable
data class VerifyOtpRequestDto(
    val mobile: String,
    val otpCode: String
)

@Serializable
data class UserProfileDto(
    val id: String,
    val phoneNumber: String,
    val nickName: String = "",
    val avatar: String = "",
    val score: Int = 0
)

@Serializable
data class AuthResponseDto(
    val token: String,
    val user: UserProfileDto
)
