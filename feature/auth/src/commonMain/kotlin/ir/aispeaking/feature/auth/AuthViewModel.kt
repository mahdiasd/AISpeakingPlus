package ir.aispeaking.feature.auth

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.usecase.onboarding.SetOnboardingUseCase
import ir.aispeaking.domain.usecase.user.auth.SendOtpUseCase
import ir.aispeaking.domain.usecase.user.auth.VerifyOtpUseCase
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.otp_code_is_empty
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.sharedui.ui.validation.ValidationStatus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import kotlin.random.Random

@KoinViewModel
class AuthViewModel(
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val setOnboardingUseCase: SetOnboardingUseCase,
) : BaseViewModel<AuthUiState, AuthUiEvent>() {

    init {
        setOnBoarding()
    }

    private fun validateInput(): Boolean {
        setState { copy(mobile = mobile.copy(shouldValidate = true)) }
        when (currentState.screenMode) {
            AuthScreenMode.WaitingForMobile -> {
                return when {
                    currentState.mobile.validate() is ValidationStatus.Invalid -> {
                        setState { copy(mobile = mobile.copy(shouldValidate = true)) }
                        false
                    }
                    else -> true
                }
            }

            AuthScreenMode.WaitingForOtpCode -> {
                val invalid = currentState.otpCodes.any { it.copy(shouldValidate = true).validate() is ValidationStatus.Invalid }
                return if (invalid) {
                    setUiMessage(UiMessage(intValue = Res.string.otp_code_is_empty))
                    false
                } else {
                    true
                }
            }
        }
    }

    private fun sendOtp() {
        setState { copy(isBtnLoading = true) }
        viewModelScope.launch {
            sendOtpUseCase(currentState.mobile.value).collect {
                setState { copy(isBtnLoading = false) }
                it.onSuccess {
                    setState {
                        copy(
                            screenMode = AuthScreenMode.WaitingForOtpCode,
                            countDownCounterKey = Random.nextInt().toString()
                        )
                    }
                }.onFailure { apiError ->
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun verifyOtp() {
        setState { copy(isBtnLoading = true) }
        viewModelScope.launch {
            verifyOtpUseCase(
                mobile = currentState.mobile.value,
                code = currentState.otpCodes.joinToString("") { it.value }
            ).collect {
                setState { copy(isBtnLoading = false) }
                it.onSuccess {
                    setUiNavigation(AuthUiNavigation.ToMain)
                }.onFailure { apiError ->
                    if (apiError is NetworkError.NotFound) {
                        setUiNavigation(AuthUiNavigation.ToRegister(currentState.mobile.value))
                    } else {
                        setUiMessage(apiError.toUiMessage())
                    }
                }
            }
        }
    }

    override fun createInitialState() = AuthUiState()

    override fun onTriggerEvent(event: AuthUiEvent) {
        when (event) {
            is AuthUiEvent.ChangePasswordVisibility -> {
                setState { copy(isPasswordHide = event.hide) }
            }

            is AuthUiEvent.OnChangeMobile -> {
                setState {
                    copy(
                        mobile = mobile.copy(value = event.newText, shouldValidate = false)
                    )
                }
            }

            is AuthUiEvent.OnSendBtnClick -> {
                if (validateInput()) {
                    when (currentState.screenMode) {
                        AuthScreenMode.WaitingForMobile -> sendOtp()
                        AuthScreenMode.WaitingForOtpCode -> verifyOtp()
                    }
                }
            }

            is AuthUiEvent.OnChangeOTP -> {
                setState {
                    copy(otpCodes = otpCodes.mapIndexed { index, otpCode ->
                        if (index == event.indexInList) otpCode.copy(
                            value = event.newText,
                            shouldValidate = false
                        )
                        else otpCode
                    }.toImmutableList())
                }
            }

            is AuthUiEvent.OnRetrySendingSms -> {
                sendOtp()
            }

            is AuthUiEvent.OnChangeScreenMode -> {
                setState { copy(screenMode = event.screenMode) }
            }

            is AuthUiEvent.PrivacyPolicyDialog -> {
                setState { copy(showPrivacyDialog = event.show) }
            }
        }
    }

    private fun setOnBoarding() {
        viewModelScope.launch {
            setOnboardingUseCase.invoke()
        }
    }
}