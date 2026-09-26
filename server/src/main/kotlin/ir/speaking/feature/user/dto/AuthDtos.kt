package ir.speaking.feature.user.dto

import kotlinx.serialization.Serializable

import ir.speaking.core.utils.AppUtils

@Serializable
data class SendOtpRequest(
    val mobile: String? = null,
    val phoneNumber: String? = null,
    val phone: String? = null,
    val mobileNumber: String? = null,
    val mobile_number: String? = null
) {
    fun getNormalizedMobile(): String? {
        val raw = mobile ?: phoneNumber ?: phone ?: mobileNumber ?: mobile_number
        return AppUtils.normalizeMobileNumber(raw)
    }
}

@Serializable
data class SendOtpResponse(
    val mobile: String,
    val expiresInSeconds: Int = 120
)

@Serializable
data class VerifyOtpRequest(
    val mobile: String? = null,
    val phoneNumber: String? = null,
    val phone: String? = null,
    val mobileNumber: String? = null,
    val mobile_number: String? = null,
    val otpCode: String? = null,
    val code: String? = null,
    val otp: String? = null
) {
    fun getNormalizedMobile(): String? {
        val raw = mobile ?: phoneNumber ?: phone ?: mobileNumber ?: mobile_number
        return AppUtils.normalizeMobileNumber(raw)
    }

    fun getNormalizedOtpCode(): String? {
        val raw = otpCode ?: code ?: otp
        return raw?.let { AppUtils.normalizeDigits(it.trim()) }
    }
}

@Serializable
data class UserProfileResponse(
    val id: String,
    val phoneNumber: String,
    val nickName: String,
    val avatar: String,
    val score: Int
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserProfileResponse
)
