package ir.aispeaking.auth

import ir.aispeaking.domain.model.user.User

enum class LoginStep {
    ENTER_PHONE,
    ENTER_OTP
}

data class LoginUiState(
    val step: LoginStep = LoginStep.ENTER_PHONE,
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val phoneError: String? = null,
    val otpError: String? = null,
    val generalError: String? = null,
    val countdownSeconds: Int = 0,
    val canResendOtp: Boolean = false
)

sealed interface LoginIntent {
    data class OnPhoneChanged(val phone: String) : LoginIntent
    data class OnOtpChanged(val otp: String) : LoginIntent
    data object SendOtpClicked : LoginIntent
    data object VerifyOtpClicked : LoginIntent
    data object ResendOtpClicked : LoginIntent
    data object ChangePhoneClicked : LoginIntent
    data object SkipGuestClicked : LoginIntent
    data object DismissError : LoginIntent
}

sealed interface LoginEffect {
    data class LoginSuccess(val user: User) : LoginEffect
    data object NavigateToMain : LoginEffect
    data class ShowToast(val message: String) : LoginEffect
}
