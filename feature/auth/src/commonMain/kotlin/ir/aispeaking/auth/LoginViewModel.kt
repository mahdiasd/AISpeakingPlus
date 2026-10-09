package ir.aispeaking.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.usecase.auth.SendOtpUseCase
import ir.aispeaking.domain.usecase.auth.VerifyOtpUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class LoginViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _effect = Channel<LoginEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    private var timerJob: Job? = null

    fun processIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnPhoneChanged -> onPhoneChanged(intent.phone)
            is LoginIntent.OnOtpChanged -> onOtpChanged(intent.otp)
            is LoginIntent.SendOtpClicked -> sendOtp()
            is LoginIntent.VerifyOtpClicked -> verifyOtp()
            is LoginIntent.ResendOtpClicked -> resendOtp()
            is LoginIntent.ChangePhoneClicked -> changePhone()
            is LoginIntent.SkipGuestClicked -> skipToGuest()
            is LoginIntent.DismissError -> dismissError()
        }
    }

    private fun onPhoneChanged(phone: String) {
        val filtered = phone.normalizeDigits().filter { it.isDigit() }.take(11)
        _uiState.update {
            it.copy(
                phoneNumber = filtered,
                phoneError = null,
                generalError = null
            )
        }
    }

    private fun onOtpChanged(otp: String) {
        val filtered = otp.normalizeDigits().filter { it.isDigit() }.take(6)
        _uiState.update {
            it.copy(
                otpCode = filtered,
                otpError = null,
                generalError = null
            )
        }
    }

    private fun String.normalizeDigits(): String {
        val builder = StringBuilder(length)
        for (ch in this) {
            when (ch) {
                in '۰'..'۹' -> builder.append((ch.code - '۰'.code + '0'.code).toChar())
                in '٠'..'٩' -> builder.append((ch.code - '٠'.code + '0'.code).toChar())
                else -> builder.append(ch)
            }
        }
        return builder.toString()
    }

    private fun isValidIranianPhone(phone: String): Boolean {
        return phone.length == 11 && phone.startsWith("09")
    }

    private fun sendOtp() {
        val phone = _uiState.value.phoneNumber.trim()
        if (!isValidIranianPhone(phone)) {
            _uiState.update {
                it.copy(phoneError = "لطفاً شماره موبایل معتبر ۱۱ رقمی (مانند 09123456789) وارد کنید.")
            }
            return
        }

        // Demo support for end-to-end testing of OTP step without backend
        if (phone == "09000000000") {
            val expiresIn = 60
            _uiState.update {
                it.copy(
                    isLoading = false,
                    step = LoginStep.ENTER_OTP,
                    countdownSeconds = expiresIn,
                    canResendOtp = false,
                    otpCode = "",
                    otpError = null
                )
            }
            startCountdown(expiresIn)
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, phoneError = null, generalError = null) }
            when (val result = sendOtpUseCase(phone)) {
                is DataResult.Success -> {
                    val expiresIn = result.data.coerceAtLeast(30)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = LoginStep.ENTER_OTP,
                            countdownSeconds = expiresIn,
                            canResendOtp = false,
                            otpCode = "",
                            otpError = null
                        )
                    }
                    startCountdown(expiresIn)
                }
                is DataResult.Failure -> {
                    val errorMsg = result.appError.getErrorMessage().ifBlank { "خطا در ارسال کد تایید" }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            generalError = errorMsg
                        )
                    }
                }
            }
        }
    }

    private fun resendOtp() {
        if (!_uiState.value.canResendOtp) return
        sendOtp()
    }

    private fun verifyOtp() {
        val phone = _uiState.value.phoneNumber.trim()
        val otp = _uiState.value.otpCode.trim()

        if (otp.length < 4) {
            _uiState.update { it.copy(otpError = "لطفاً کد تایید را کامل وارد نمایید.") }
            return
        }

        viewModelScope.launch {
            // Demo verification for testing
            if (phone == "09000000000") {
                if (otp == "12345") {
                    timerJob?.cancel()
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.LoginSuccess(ir.aispeaking.domain.model.user.User(uid = "demo_user", nickName = "کاربر آزمایشی", mobile = phone)))
                    _effect.send(LoginEffect.NavigateToMain)
                } else {
                    _uiState.update { it.copy(isLoading = false, otpError = "کد تایید اشتباه است (برای تست 12345 را وارد نمایید)") }
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, otpError = null, generalError = null) }
            when (val result = verifyOtpUseCase(phone, otp)) {
                is DataResult.Success -> {
                    timerJob?.cancel()
                    _uiState.update { it.copy(isLoading = false) }
                    _effect.send(LoginEffect.LoginSuccess(result.data))
                    _effect.send(LoginEffect.NavigateToMain)
                }
                is DataResult.Failure -> {
                    val errorMsg = result.appError.getErrorMessage().ifBlank { "کد تایید اشتباه است یا منقضی شده است" }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            otpError = errorMsg
                        )
                    }
                }
            }
        }
    }

    private fun changePhone() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                step = LoginStep.ENTER_PHONE,
                otpCode = "",
                otpError = null,
                generalError = null,
                countdownSeconds = 0,
                canResendOtp = false
            )
        }
    }

    private fun skipToGuest() {
        viewModelScope.launch {
            _effect.send(LoginEffect.NavigateToMain)
        }
    }

    private fun dismissError() {
        _uiState.update { it.copy(generalError = null, phoneError = null, otpError = null) }
    }

    private fun startCountdown(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var current = totalSeconds
            while (current > 0) {
                delay(1000)
                current--
                _uiState.update { it.copy(countdownSeconds = current, canResendOtp = current == 0) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
