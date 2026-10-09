package ir.aispeaking.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.auth_vector
import ir.aispeaking.sharedui.dialog_message_vector
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ic_phone
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ui.core.loading.DotLoading
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Route composable connecting LoginViewModel to pure LoginScreen.
 */
@Composable
fun LoginRoute(
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

    LoginScreen(
        modifier = modifier,
        uiState = uiState,
        onPhoneChanged = { viewModel.processIntent(LoginIntent.OnPhoneChanged(it)) },
        onOtpChanged = { viewModel.processIntent(LoginIntent.OnOtpChanged(it)) },
        onSendOtp = {
            focusManager.clearFocus()
            viewModel.processIntent(LoginIntent.SendOtpClicked)
        },
        onVerifyOtp = {
            focusManager.clearFocus()
            viewModel.processIntent(LoginIntent.VerifyOtpClicked)
        },
        onResendOtp = { viewModel.processIntent(LoginIntent.ResendOtpClicked) },
        onChangePhone = { viewModel.processIntent(LoginIntent.ChangePhoneClicked) },
        onSkipGuest = { viewModel.processIntent(LoginIntent.SkipGuestClicked) },
        onDismissError = { viewModel.processIntent(LoginIntent.DismissError) }
    )
}

/**
 * Backwards compatible overload for LoginScreen with ViewModel injection.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToMain: (User?) -> Unit = {}
) {
    LoginRoute(
        modifier = modifier,
        viewModel = viewModel,
        onNavigateToMain = onNavigateToMain
    )
}

/**
 * Pure stateless Composable for Login Screen with Disney-inspired character art and playful layout.
 */
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    uiState: LoginUiState,
    onPhoneChanged: (String) -> Unit,
    onOtpChanged: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onChangePhone: () -> Unit,
    onSkipGuest: () -> Unit,
    onDismissError: () -> Unit
) {
    // Gentle floating animation for character illustration
    val infiniteTransition = rememberInfiniteTransition(label = "CharacterFloat")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CharY"
    )

    // Outer responsive background
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AppTheme.colors.surface,
                        AppTheme.colors.surfaceContainerLow,
                        AppTheme.colors.surface
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Enforce mobile-sized width when rendered on wide screens (desktop/laptop/tablet)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Top Disney Hero Character Section
                CharacterHeroSection(
                    step = uiState.step,
                    floatOffset = floatAnim
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive Auth Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(28.dp),
                            spotColor = AppTheme.colors.primary.copy(alpha = 0.18f)
                        ),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.surfaceContainerLowest
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 26.dp),
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
                            label = "LoginStepAnimation"
                        ) { step ->
                            when (step) {
                                LoginStep.ENTER_PHONE -> {
                                    PhoneInputStep(
                                        uiState = uiState,
                                        onPhoneChanged = onPhoneChanged,
                                        onSendOtp = onSendOtp,
                                        onSkipGuest = onSkipGuest
                                    )
                                }

                                LoginStep.ENTER_OTP -> {
                                    OtpInputStep(
                                        uiState = uiState,
                                        onOtpChanged = onOtpChanged,
                                        onVerifyOtp = onVerifyOtp,
                                        onChangePhone = onChangePhone,
                                        onResendOtp = onResendOtp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Trust & Security Footnote
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "🔒 ورود ایمن و سریع با کد یکبارمصرف پیامکی",
                        style = AppTheme.typography.labelSmall.copy(
                            fontFamily = AppTheme.typography.persianRegular
                        ),
                        fontSize = 11.sp,
                        color = AppTheme.colors.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Top character hero featuring Disney/Pixar 3D illustrations with lively speech bubbles.
 */
@Composable
private fun CharacterHeroSection(
    step: LoginStep,
    floatOffset: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = floatOffset.dp)
    ) {
        // Speech Bubble
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.primaryContainer.copy(alpha = 0.85f))
                .border(
                    width = 1.dp,
                    color = AppTheme.colors.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = if (step == LoginStep.ENTER_PHONE) {
                    "سلام دوست من! 👋\nشماره موبایلت رو بنویس تا با هم شروع کنیم!"
                } else {
                    "عالیه! 🎉\nکد ارسال شده به گوشیت رو وارد کن تا بریم داخل!"
                },
                style = AppTheme.typography.bodyMediumBold.copy(
                    fontFamily = AppTheme.typography.persianBold
                ),
                fontSize = 13.sp,
                color = AppTheme.colors.onPrimaryContainer,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Disney 3D Pixar Character Image
        Box(
            modifier = Modifier.size(150.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    if (step == LoginStep.ENTER_PHONE) Res.drawable.auth_vector
                    else Res.drawable.dialog_message_vector
                ),
                contentDescription = "Disney Character",
                modifier = Modifier.size(145.dp)
            )
        }
    }
}

/**
 * Step 1: Phone number input and Guest access.
 */
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
            text = "ورود / عضویت",
            style = AppTheme.typography.headlineBold.copy(
                fontFamily = AppTheme.typography.persianBold
            ),
            fontSize = 22.sp,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "برای شروع ماجراجویی مکالمه انگلیسی، شماره موبایل خود را وارد نمایید.",
            style = AppTheme.typography.bodyMedium.copy(
                fontFamily = AppTheme.typography.persianRegular
            ),
            fontSize = 13.sp,
            color = AppTheme.colors.outline,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phone Input with Persian Direction & Leading Icon
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = onPhoneChanged,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(
                        text = "شماره موبایل",
                        style = AppTheme.typography.bodyMedium.copy(
                            fontFamily = AppTheme.typography.persianRegular
                        )
                    )
                },
                placeholder = {
                    Text(
                        text = "۰۹۱۲۳۴۵۶۷۸۹",
                        style = AppTheme.typography.bodyMedium.copy(
                            fontFamily = AppTheme.typography.persianRegular
                        ),
                        color = AppTheme.colors.outlineVariant
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.ic_phone),
                        contentDescription = "Phone Icon",
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.phoneNumber.isNotEmpty()) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = "Clear",
                            tint = AppTheme.colors.outline,
                            modifier = Modifier
                                .size(18.dp)
                                .clickable { onPhoneChanged("") }
                        )
                    }
                },
                isError = uiState.phoneError != null || uiState.generalError != null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { if (!uiState.isLoading) onSendOtp() }
                ),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AppTheme.colors.onSurface,
                    unfocusedTextColor = AppTheme.colors.onSurface,
                    focusedBorderColor = AppTheme.colors.primary,
                    unfocusedBorderColor = AppTheme.colors.outlineVariant,
                    errorBorderColor = AppTheme.colors.error,
                    focusedContainerColor = AppTheme.colors.surfaceContainerLowest,
                    unfocusedContainerColor = AppTheme.colors.surfaceContainerLowest
                ),
                textStyle = AppTheme.typography.bodyLargeBold.copy(
                    fontFamily = AppTheme.typography.persianBold,
                    textDirection = TextDirection.Ltr
                )
            )
        }

        // Inline Error Animation
        AnimatedVisibility(visible = uiState.phoneError != null) {
            Text(
                text = uiState.phoneError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium.copy(
                    fontFamily = AppTheme.typography.persianRegular
                ),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp),
                textAlign = TextAlign.Start
            )
        }

        AnimatedVisibility(visible = uiState.generalError != null) {
            Text(
                text = uiState.generalError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium.copy(
                    fontFamily = AppTheme.typography.persianRegular
                ),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp),
                textAlign = TextAlign.Start
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Send OTP Button
        Button(
            onClick = onSendOtp,
            enabled = !uiState.isLoading && uiState.phoneNumber.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = if (!uiState.isLoading && uiState.phoneNumber.isNotBlank()) 6.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = AppTheme.colors.primary.copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                disabledContainerColor = AppTheme.colors.outlineVariant
            )
        ) {
            if (uiState.isLoading) {
                DotLoading(
                    dotColor = AppTheme.colors.onPrimary,
                    dotSize = 10.dp
                )
            } else {
                Text(
                    text = "ارسال کد تایید پیامکی ✨",
                    style = AppTheme.typography.bodyLargeBold.copy(
                        fontFamily = AppTheme.typography.persianBold
                    ),
                    fontSize = 15.sp,
                    color = AppTheme.colors.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Guest Access Button
        OutlinedButton(
            onClick = onSkipGuest,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                AppTheme.colors.primary.copy(alpha = 0.5f)
            ),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = AppTheme.colors.primaryContainer.copy(alpha = 0.25f)
            )
        ) {
            Text(
                text = "ورود به عنوان مهمان (آزمایشی) 🚀",
                style = AppTheme.typography.bodyMediumBold.copy(
                    fontFamily = AppTheme.typography.persianBold
                ),
                fontSize = 13.sp,
                color = AppTheme.colors.primary
            )
        }
    }
}

/**
 * Step 2: OTP Verification with segmented digit input boxes and resend countdown.
 */
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
            text = "کد تایید پیامکی",
            style = AppTheme.typography.headlineBold.copy(
                fontFamily = AppTheme.typography.persianBold
            ),
            fontSize = 22.sp,
            color = AppTheme.colors.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "کد ارسال شده به شماره زیر را وارد نمایید:",
            style = AppTheme.typography.bodyMedium.copy(
                fontFamily = AppTheme.typography.persianRegular
            ),
            fontSize = 13.sp,
            color = AppTheme.colors.outline,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Phone number pill with edit action
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(AppTheme.colors.surfaceContainerLow)
                .clickable { onChangePhone() }
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = uiState.phoneNumber,
                style = AppTheme.typography.bodyMediumBold,
                fontSize = 14.sp,
                color = AppTheme.colors.primary
            )

            Spacer(modifier = Modifier.width(6.dp))

            Icon(
                painter = painterResource(Res.drawable.ic_edit),
                contentDescription = "Edit phone",
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(14.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "ویرایش",
                style = AppTheme.typography.labelSmall.copy(
                    fontFamily = AppTheme.typography.persianRegular
                ),
                fontSize = 11.sp,
                color = AppTheme.colors.outline
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Segmented OTP Digits Display
        OtpDigitBoxes(
            code = uiState.otpCode,
            onCodeChanged = onOtpChanged,
            isError = uiState.otpError != null || uiState.generalError != null,
            onDone = { if (!uiState.isLoading && uiState.otpCode.isNotBlank()) onVerifyOtp() }
        )

        // Inline Error Animation
        AnimatedVisibility(visible = uiState.otpError != null) {
            Text(
                text = uiState.otpError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium.copy(
                    fontFamily = AppTheme.typography.persianRegular
                ),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(visible = uiState.generalError != null) {
            Text(
                text = uiState.generalError ?: "",
                color = AppTheme.colors.error,
                style = AppTheme.typography.labelMedium.copy(
                    fontFamily = AppTheme.typography.persianRegular
                ),
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Verify and Enter Button
        Button(
            onClick = onVerifyOtp,
            enabled = !uiState.isLoading && uiState.otpCode.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .shadow(
                    elevation = if (!uiState.isLoading && uiState.otpCode.isNotBlank()) 6.dp else 0.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = AppTheme.colors.primary.copy(alpha = 0.4f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.primary,
                disabledContainerColor = AppTheme.colors.outlineVariant
            )
        ) {
            if (uiState.isLoading) {
                DotLoading(
                    dotColor = AppTheme.colors.onPrimary,
                    dotSize = 10.dp
                )
            } else {
                Text(
                    text = "تایید و ورود به ماجراجویی ✨",
                    style = AppTheme.typography.bodyLargeBold.copy(
                        fontFamily = AppTheme.typography.persianBold
                    ),
                    fontSize = 15.sp,
                    color = AppTheme.colors.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Smallest-scope leaf for countdown timer read (Rule 2 of compose-ui)
        OtpResendSection(
            countdownSeconds = uiState.countdownSeconds,
            canResendOtp = uiState.canResendOtp,
            onResendOtp = onResendOtp
        )
    }
}

/**
 * Modern segmented OTP input box row using standard decorationBox pattern.
 */
@Composable
private fun OtpDigitBoxes(
    code: String,
    onCodeChanged: (String) -> Unit,
    isError: Boolean,
    onDone: () -> Unit,
    boxCount: Int = 5
) {
    BasicTextField(
        value = code,
        onValueChange = { newText ->
            val digits = newText.filter { it.isDigit() }
            if (digits.length <= boxCount) {
                onCodeChanged(digits)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone() }
        ),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                // Must invoke innerTextField so Compose attaches text input handler
                Box(modifier = Modifier.size(0.dp)) {
                    innerTextField()
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(boxCount) { index ->
                            val char = code.getOrNull(index)?.toString() ?: ""
                            val isFocused = code.length == index || (index == boxCount - 1 && code.length >= boxCount)

                            val borderColor = when {
                                isError -> AppTheme.colors.error
                                isFocused -> AppTheme.colors.primary
                                char.isNotEmpty() -> AppTheme.colors.primary.copy(alpha = 0.5f)
                                else -> AppTheme.colors.outlineVariant
                            }

                            val containerColor = when {
                                isFocused -> AppTheme.colors.primaryContainer.copy(alpha = 0.35f)
                                char.isNotEmpty() -> AppTheme.colors.surfaceContainerLowest
                                else -> AppTheme.colors.surfaceContainerLow
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 58.dp)
                                    .shadow(
                                        elevation = if (isFocused) 6.dp else 1.dp,
                                        shape = RoundedCornerShape(14.dp),
                                        spotColor = AppTheme.colors.primary.copy(alpha = 0.2f)
                                    )
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(containerColor)
                                    .border(
                                        width = if (isFocused) 2.dp else 1.dp,
                                        color = borderColor,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    style = AppTheme.typography.headlineBold,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

/**
 * Smallest scope leaf for the ticking countdown seconds timer (Rule 2).
 */
@Composable
private fun OtpResendSection(
    countdownSeconds: Int,
    canResendOtp: Boolean,
    onResendOtp: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (canResendOtp) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onResendOtp() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_refresh),
                    contentDescription = "Resend",
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "ارسال مجدد کد پیامکی",
                    style = AppTheme.typography.bodyMediumBold.copy(
                        fontFamily = AppTheme.typography.persianBold
                    ),
                    fontSize = 13.sp,
                    color = AppTheme.colors.primary
                )
            }
        } else {
            Text(
                text = "⏱ ارسال مجدد کد تا $countdownSeconds ثانیه دیگر",
                style = AppTheme.typography.labelMedium.copy(
                    fontFamily = AppTheme.typography.persianRegular
                ),
                fontSize = 12.sp,
                color = AppTheme.colors.outline
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.PreviewLightDark
@Composable
private fun LoginScreenPhonePreview() {
    AppTheme {
        LoginScreen(
            uiState = LoginUiState(step = LoginStep.ENTER_PHONE),
            onPhoneChanged = {},
            onOtpChanged = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onResendOtp = {},
            onChangePhone = {},
            onSkipGuest = {},
            onDismissError = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun LoginScreenOtpPreview() {
    AppTheme {
        LoginScreen(
            uiState = LoginUiState(
                step = LoginStep.ENTER_OTP,
                phoneNumber = "09123456789",
                countdownSeconds = 45
            ),
            onPhoneChanged = {},
            onOtpChanged = {},
            onSendOtp = {},
            onVerifyOtp = {},
            onResendOtp = {},
            onChangePhone = {},
            onSkipGuest = {},
            onDismissError = {}
        )
    }
}
