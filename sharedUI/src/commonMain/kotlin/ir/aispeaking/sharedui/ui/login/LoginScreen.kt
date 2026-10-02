package ir.aispeaking.sharedui.ui.login

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToMain: (User?) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is LoginEffect.LoginSuccess -> onNavigateToMain(effect.user)
                is LoginEffect.NavigateToMain -> onNavigateToMain(null)
                is LoginEffect.ShowToast -> {
                    // Handled if toast/snackbar provided
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.surfaceContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = uiState.step,
                    transitionSpec = {
                        if (targetState == LoginStep.ENTER_OTP) {
                            (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> -width } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                    (slideOutHorizontally { width -> width } + fadeOut())
                        }
                    },
                    label = "LoginStepTransition"
                ) { step ->
                    when (step) {
                        LoginStep.ENTER_PHONE -> {
                            PhoneInputStep(
                                uiState = uiState,
                                onPhoneChanged = { viewModel.processIntent(LoginIntent.OnPhoneChanged(it)) },
                                onSendOtp = {
                                    focusManager.clearFocus()
                                    viewModel.processIntent(LoginIntent.SendOtpClicked)
                                },
                                onSkipGuest = {
                                    viewModel.processIntent(LoginIntent.SkipGuestClicked)
                                }
                            )
                        }

                        LoginStep.ENTER_OTP -> {
                            OtpInputStep(
                                uiState = uiState,
                                onOtpChanged = { viewModel.processIntent(LoginIntent.OnOtpChanged(it)) },
                                onVerifyOtp = {
                                    focusManager.clearFocus()
                                    viewModel.processIntent(LoginIntent.VerifyOtpClicked)
                                },
                                onChangePhone = {
                                    viewModel.processIntent(LoginIntent.ChangePhoneClicked)
                                },
                                onResendOtp = {
                                    viewModel.processIntent(LoginIntent.ResendOtpClicked)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhoneInputStep(
    uiState: LoginUiState,
    onPhoneChanged: (String) -> Unit,
    onSendOtp: () -> Unit,
    onSkipGuest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ورود / ثبت‌نام",
            style = AppTheme.typography.headlineBold,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "برای ورود یا ساخت حساب کاربری، لطفاً شماره موبایل خود را وارد نمایید.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = onPhoneChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("شماره موبایل") },
            placeholder = { Text("09123456789") },
            singleLine = true,
            isError = uiState.phoneError != null || uiState.generalError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (!uiState.isLoading) onSendOtp() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.colors.onSurface,
                unfocusedTextColor = AppTheme.colors.onSurface,
                focusedBorderColor = AppTheme.colors.primary,
                unfocusedBorderColor = AppTheme.colors.outlineVariant,
                errorBorderColor = AppTheme.colors.error
            )
        )

        AnimatedVisibility(visible = uiState.phoneError != null) {
            Text(
                text = uiState.phoneError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp)
            )
        }

        AnimatedVisibility(visible = uiState.generalError != null) {
            Text(
                text = uiState.generalError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSendOtp,
            enabled = !uiState.isLoading && uiState.phoneNumber.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                disabledContainerColor = AppTheme.colors.outlineVariant
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = AppTheme.colors.onPrimary,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "ارسال کد تایید پیامکی",
                    style = AppTheme.typography.titleBold,
                    color = AppTheme.colors.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onSkipGuest) {
            Text(
                text = "ادامه به عنوان مهمان",
                style = AppTheme.typography.bodyMediumBold,
                color = AppTheme.colors.primary
            )
        }
    }
}

@Composable
private fun OtpInputStep(
    uiState: LoginUiState,
    onOtpChanged: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onChangePhone: () -> Unit,
    onResendOtp: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "کد تایید را وارد کنید",
            style = AppTheme.typography.headlineBold,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "کد تایید به شماره ${uiState.phoneNumber} پیامک شد.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = uiState.otpCode,
            onValueChange = onOtpChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("کد تایید") },
            placeholder = { Text("12345") },
            singleLine = true,
            isError = uiState.otpError != null || uiState.generalError != null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (!uiState.isLoading) onVerifyOtp() }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = AppTheme.colors.onSurface,
                unfocusedTextColor = AppTheme.colors.onSurface,
                focusedBorderColor = AppTheme.colors.primary,
                unfocusedBorderColor = AppTheme.colors.outlineVariant,
                errorBorderColor = AppTheme.colors.error
            )
        )

        AnimatedVisibility(visible = uiState.otpError != null) {
            Text(
                text = uiState.otpError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp)
            )
        }

        AnimatedVisibility(visible = uiState.generalError != null) {
            Text(
                text = uiState.generalError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtp,
            enabled = !uiState.isLoading && uiState.otpCode.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                disabledContainerColor = AppTheme.colors.outlineVariant
            )
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = AppTheme.colors.onPrimary,
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = "تایید و ورود",
                    style = AppTheme.typography.titleBold,
                    color = AppTheme.colors.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ویرایش شماره موبایل",
                style = AppTheme.typography.labelMediumBold,
                color = AppTheme.colors.primary,
                modifier = Modifier.clickable { onChangePhone() }
            )

            if (uiState.canResendOtp) {
                Text(
                    text = "ارسال مجدد کد",
                    style = AppTheme.typography.labelMediumBold,
                    color = AppTheme.colors.primary,
                    modifier = Modifier.clickable { onResendOtp() }
                )
            } else {
                Text(
                    text = "${uiState.countdownSeconds} ثانیه تا ارسال مجدد",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.outline
                )
            }
        }
    }
}
