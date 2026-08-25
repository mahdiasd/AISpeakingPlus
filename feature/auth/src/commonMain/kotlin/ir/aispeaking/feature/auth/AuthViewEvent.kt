package ir.aispeaking.feature.auth

import androidx.compose.runtime.Stable
import ir.aispeaking.feature.auth.inputs.Mobile
import ir.aispeaking.feature.auth.inputs.OtpCode
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlin.random.Random

@Stable
data class AuthUiState(
    val isBtnLoading: Boolean = false,
    val screenMode: AuthScreenMode = AuthScreenMode.WaitingForMobile,
    val mobile: Mobile = Mobile(value = ""),
    val otpCodes: ImmutableList<OtpCode> = immutableListOf(
        OtpCode(value = ""),
        OtpCode(value = ""),
        OtpCode(value = ""),
        OtpCode(value = ""),
        OtpCode(value = "")
    ),
    val isPasswordHide: Boolean = true,
    val showPrivacyDialog: Boolean = false,
    /**
     * Use for recompose count down timer and reset it
     */
    val countDownCounterKey: String = Random.nextInt().toString(),
) : UiState

enum class AuthScreenMode {
    WaitingForMobile,
    WaitingForOtpCode
}

sealed class AuthUiEvent : UiEvent {
    data class OnChangeMobile(val newText: String) : AuthUiEvent()
    data class PrivacyPolicyDialog(val show: Boolean) : AuthUiEvent()
    data class OnChangeOTP(val newText: String, val indexInList: Int) : AuthUiEvent()
    data class ChangePasswordVisibility(val hide: Boolean) : AuthUiEvent()
    data class OnChangeScreenMode(val screenMode: AuthScreenMode) : AuthUiEvent()
    data object OnSendBtnClick : AuthUiEvent()
    data object OnRetrySendingSms : AuthUiEvent()
}

sealed class AuthUiNavigation : UiNavigation {
    data object ToMain : AuthUiNavigation()
    data class ToRegister(val mobile: String) : AuthUiNavigation()
}

typealias OnAction = (AuthUiEvent) -> Unit