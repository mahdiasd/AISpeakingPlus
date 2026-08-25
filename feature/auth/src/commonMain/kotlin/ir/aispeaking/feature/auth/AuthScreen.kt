package ir.aispeaking.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.aispeaking.feature.auth.component.CountdownTimer
import ir.aispeaking.feature.auth.component.OtpInputsSection
import ir.aispeaking.feature.auth.component.PrivacyPolicyDialog
import ir.aispeaking.feature.auth.inputs.Mobile
import ir.aispeaking.feature.auth.inputs.OtpCode
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.auth_screen_btn_waiting_mobile
import ir.aispeaking.sharedui.auth_screen_btn_waiting_otp
import ir.aispeaking.sharedui.auth_screen_change_it
import ir.aispeaking.sharedui.auth_screen_description
import ir.aispeaking.sharedui.auth_screen_description_otp_mode
import ir.aispeaking.sharedui.auth_screen_title
import ir.aispeaking.sharedui.auth_screen_wrong_number
import ir.aispeaking.sharedui.auth_vector
import ir.aispeaking.sharedui.ic_phone
import ir.aispeaking.sharedui.ui.core.button.AppButton
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppTextField
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.core.ui_message.UiMessageScreen
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.validation.errorMessage
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    vm: AuthViewModel = koinViewModel<AuthViewModel>(),
    navigateToRegister: (String) -> Unit,
    navigateToMain: () -> Unit,
) {
    val uiNavigation by vm.uiNavigation.collectAsStateWithLifecycle(null)
    val uiState = vm.uiState.collectAsState().value

    AuthScreenContent(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.ime)
            .baseModifier()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        mobile = uiState.mobile,
        onAction = { vm.onTriggerEvent(it) },
        countDownCounterKey = uiState.countDownCounterKey,
        isBtnLoading = uiState.isBtnLoading,
        screenMode = uiState.screenMode,
        otpCodes = uiState.otpCodes
    )

    UiMessageScreen(shared = vm.uiMessage)

    LaunchedEffect(uiNavigation) {
        when (uiNavigation) {
            is AuthUiNavigation.ToMain -> navigateToMain()
            is AuthUiNavigation.ToRegister -> navigateToRegister((uiNavigation as AuthUiNavigation.ToRegister).mobile)
            null -> {}
        }
    }

    PrivacyPolicyDialog(
        show = uiState.showPrivacyDialog,
        onDismiss = { vm.onTriggerEvent(AuthUiEvent.PrivacyPolicyDialog(false)) }
    )

    // SMS Retriever and Context dependencies have been completely removed for CMP commonMain.
}

@Composable
fun AuthScreenContent(
    modifier: Modifier = Modifier,
    mobile: Mobile,
    otpCodes: ImmutableList<OtpCode>,
    isBtnLoading: Boolean = false,
    screenMode: AuthScreenMode = AuthScreenMode.WaitingForMobile,
    countDownCounterKey: String = "",
    onAction: OnAction,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(mobile.value) {
        if (mobile.value.length == 11) keyboardController?.hide()
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Image(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp)
                .aspectRatio(1f),
            painter = painterResource(Res.drawable.auth_vector),
            contentDescription = null
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically
            )
        ) {
            TitleBoldText(
                text = stringResource(Res.string.auth_screen_title),
                color = AppTheme.colors.primary
            )

            VerticalSpace(24.dp)

            BodyMediumText(
                text = stringResource(
                    when (screenMode) {
                        AuthScreenMode.WaitingForMobile -> Res.string.auth_screen_description
                        AuthScreenMode.WaitingForOtpCode -> Res.string.auth_screen_description_otp_mode
                    }
                ),
                color = AppTheme.colors.outline
            )

            AnimatedContent(
                targetState = screenMode,
                label = "ScreenModeAnimation"
            ) { mode ->
                when (mode) {
                    AuthScreenMode.WaitingForMobile -> {
                        val bringIntoViewRequester = remember { BringIntoViewRequester() }
                        AppTextField(
                            modifier = Modifier
                                .bringIntoViewRequester(bringIntoViewRequester)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            value = mobile.value,
                            onValueChange = {
                                onAction(AuthUiEvent.OnChangeMobile(it))
                            },
                            hint = stringResource(mobile.hint),
                            textDirection = TextDirection.Ltr,
                            showClearIcon = false,
                            textStyle = AppTheme.typography.bodyMedium.copy(
                                letterSpacing = 2.sp,
                            ),
                            errorText = mobile.validate().errorMessage()?.let { stringResource(it) },
                            placeholderTextStyle = AppTheme.typography.bodyLarge.copy(
                                letterSpacing = 8.sp,
                                color = AppTheme.colors.outline,
                            ),
                            leadingIcon = {
                                Row(
                                    modifier = Modifier,
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(
                                        8.dp,
                                        alignment = Alignment.CenterHorizontally
                                    )
                                ) {
                                    AppIcon(
                                        icon = Res.drawable.ic_phone,
                                        contentDescription = "enter mobile number",
                                        tint = AppTheme.colors.outlineVariant
                                    )
                                    VerticalDivider(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(20.dp),
                                        color = AppTheme.colors.outlineVariant
                                    )
                                }
                            },
                            trailingIcon = {
                                Spacer(modifier = Modifier.size(24.dp))
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                        )
                    }

                    AuthScreenMode.WaitingForOtpCode -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
                        ) {
                            CountdownTimer(
                                key = countDownCounterKey,
                                onRetry = { onAction(AuthUiEvent.OnRetrySendingSms) }
                            )
                            OtpInputsSection(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                otpCodes = otpCodes,
                                onAction = onAction
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
                            ) {
                                LabelMediumText(
                                    text = stringResource(Res.string.auth_screen_wrong_number)
                                )
                                LabelMediumBoldText(
                                    modifier = Modifier.animateClickable { onAction(AuthUiEvent.OnChangeScreenMode(AuthScreenMode.WaitingForMobile)) },
                                    text = stringResource(Res.string.auth_screen_change_it),
                                    color = AppTheme.colors.primary
                                )
                            }
                        }
                    }
                }
            }

            if (screenMode == AuthScreenMode.WaitingForMobile) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.CenterHorizontally)
                ) {
                    LabelSmallBoldText(
                        textAlign = TextAlign.End,
                        text = "By continuing, you agree to our", // Hardcoded text should ideally be moved to string resources later
                        color = AppTheme.colors.outline
                    )
                    LabelSmallBoldText(
                        modifier = Modifier.animateClickable {
                            onAction(AuthUiEvent.PrivacyPolicyDialog(true))
                        },
                        textAlign = TextAlign.End,
                        text = "Privacy Policy",
                        color = AppTheme.colors.primary
                    )
                }
            }
        }

        AppButton(
            modifier = Modifier
                .fillMaxWidth(),
            isLoading = isBtnLoading,
            containerColor = AppTheme.colors.primary,
            text = when (screenMode) {
                AuthScreenMode.WaitingForMobile -> Res.string.auth_screen_btn_waiting_mobile
                AuthScreenMode.WaitingForOtpCode -> Res.string.auth_screen_btn_waiting_otp
            },
            onClick = { onAction(AuthUiEvent.OnSendBtnClick) }
        )
    }
}


@Preview
@Composable
private fun AuthPreview() {
    AppTheme {
        AuthScreenContent(
            modifier = Modifier
                .baseModifier()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            mobile = Mobile(),
            onAction = {},
            otpCodes = immutableListOf(
                OtpCode(),
                OtpCode(),
                OtpCode(),
                OtpCode(),
                OtpCode()
            )
        )
    }
}

@Preview
@Composable
private fun AuthPreview2() {
    AppTheme {
        AuthScreenContent(
            modifier = Modifier
                .baseModifier()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            mobile = Mobile(),
            onAction = {},
            screenMode = AuthScreenMode.WaitingForOtpCode,
            otpCodes = immutableListOf(
                OtpCode(),
                OtpCode(),
                OtpCode(),
                OtpCode(),
                OtpCode()
            )
        )
    }
}