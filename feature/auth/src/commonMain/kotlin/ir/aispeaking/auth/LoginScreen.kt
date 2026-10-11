package ir.aispeaking.auth

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.auth_vector
import ir.aispeaking.sharedui.dialog_message_vector
import ir.aispeaking.sharedui.ic_edit
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GameTextField
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Root composable connecting LoginViewModel to pure LoginScreen.
 */
@Composable
fun LoginRootScreen(
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
 * Backward-compatible alias for LoginRootScreen.
 */
@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
    onNavigateToMain: (User?) -> Unit = {}
) = LoginRootScreen(modifier, viewModel, onNavigateToMain)

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
 * Pure stateless login screen. Dark game backdrop, a friendly character on top and a single card
 * that holds the current step (phone, then code).
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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1B1F4B), Color(0xFF0E1433), Game.Ink)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CharacterHeroSection(step = uiState.step, floatOffset = floatAnim)

                Spacer(modifier = Modifier.height(14.dp))

                GlassPanel(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    color = Game.PanelSolid,
                    border = Game.StrokeStrong
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = uiState.step,
                            transitionSpec = {
                                if (targetState == LoginStep.ENTER_OTP) {
                                    (slideInHorizontally { width -> -width } + fadeIn()) togetherWith
                                            (slideOutHorizontally { width -> width } + fadeOut())
                                } else {
                                    (slideInHorizontally { width -> width } + fadeIn()) togetherWith
                                            (slideOutHorizontally { width -> -width } + fadeOut())
                                }
                            },
                            label = "LoginStepAnimation"
                        ) { step ->
                            when (step) {
                                LoginStep.ENTER_PHONE -> PhoneInputStep(
                                    uiState = uiState,
                                    onPhoneChanged = onPhoneChanged,
                                    onSendOtp = onSendOtp,
                                    onSkipGuest = onSkipGuest
                                )

                                LoginStep.ENTER_OTP -> OtpInputStep(
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

                Spacer(modifier = Modifier.height(16.dp))

                GameText(
                    text = "ورود امن و سریع با کد یک‌بار مصرف پیامکی",
                    size = 12.sp,
                    color = Game.TextMuted,
                    align = TextAlign.Center
                )
            }
        }
    }
}

/** Friendly character with a speech bubble that says what to do on this step. */
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
        val shape = RoundedCornerShape(20.dp)
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clip(shape)
                .background(Game.PanelRaised)
                .border(1.dp, Game.StrokeStrong, shape)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            GameText(
                text = if (step == LoginStep.ENTER_PHONE) {
                    "سلام دوست من!\nشماره موبایلت رو بنویس تا با هم شروع کنیم."
                } else {
                    "عالیه!\nکدی که برات پیامک شد رو وارد کن."
                },
                size = 14.sp,
                lineHeight = 22.sp,
                bold = true,
                align = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(
                if (step == LoginStep.ENTER_PHONE) Res.drawable.auth_vector
                else Res.drawable.dialog_message_vector
            ),
            contentDescription = null,
            modifier = Modifier.size(140.dp)
        )
    }
}

/** Step 1: phone number, or continue as guest. */
@Composable
private fun PhoneInputStep(
    uiState: LoginUiState,
    onPhoneChanged: (String) -> Unit,
    onSendOtp: () -> Unit,
    onSkipGuest: () -> Unit
) {
    val canSubmit = !uiState.isLoading && uiState.phoneNumber.isNotBlank()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GameText(text = "ورود / ثبت‌نام", size = 22.sp, bold = true, align = TextAlign.Center)
        GameText(
            text = "برای شروع ماجراجویی مکالمه انگلیسی، شماره موبایلت را وارد کن.",
            size = 13.sp,
            lineHeight = 21.sp,
            color = Game.TextSecondary,
            align = TextAlign.Center
        )

        GameTextField(
            value = uiState.phoneNumber,
            onValueChange = onPhoneChanged,
            label = "شماره موبایل",
            placeholder = "09123456789",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done,
            onImeAction = { if (canSubmit) onSendOtp() },
            ltr = true,
            isError = uiState.phoneError != null
        )

        val error = uiState.phoneError ?: uiState.generalError
        if (error != null) {
            GameText(text = error, size = 12.sp, color = Game.Coral, align = TextAlign.Center)
        }

        GameButton(
            text = "ارسال کد تایید",
            onClick = onSendOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = canSubmit,
            loading = uiState.isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.weight(1f).height(1.dp).background(Game.Stroke))
            GameText(text = "یا", size = 12.sp, color = Game.TextMuted)
            Box(Modifier.weight(1f).height(1.dp).background(Game.Stroke))
        }

        GameButton(
            text = "ورود به‌عنوان مهمان",
            onClick = onSkipGuest,
            modifier = Modifier.fillMaxWidth(),
            style = GameButtonStyle.Glass,
            height = 48.dp,
            textSize = 14.sp,
            enabled = !uiState.isLoading
        )
        GameText(
            text = "بدون ثبت‌نام شروع کن؛ پیشرفتت فقط روی همین دستگاه ذخیره می‌شود.",
            size = 11.sp,
            lineHeight = 18.sp,
            color = Game.TextMuted,
            align = TextAlign.Center
        )
    }
}

/** Step 2: segmented code boxes, verify, and resend countdown. */
@Composable
private fun OtpInputStep(
    uiState: LoginUiState,
    onOtpChanged: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onChangePhone: () -> Unit,
    onResendOtp: () -> Unit
) {
    val canSubmit = !uiState.isLoading && uiState.otpCode.isNotBlank()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GameText(text = "کد تایید را وارد کن", size = 22.sp, bold = true, align = TextAlign.Center)
        GameText(
            text = "کد پیامک‌شده به این شماره را بنویس:",
            size = 13.sp,
            color = Game.TextSecondary,
            align = TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(text = uiState.phoneNumber, size = 17.sp, bold = true, latin = true)
            }
            GameChip(
                text = "ویرایش شماره",
                icon = Res.drawable.ic_edit,
                accent = Game.Sky,
                onClick = onChangePhone
            )
        }

        OtpDigitBoxes(
            code = uiState.otpCode,
            onCodeChanged = onOtpChanged,
            isError = uiState.otpError != null,
            onDone = { if (canSubmit) onVerifyOtp() }
        )

        val error = uiState.otpError ?: uiState.generalError
        if (error != null) {
            GameText(text = error, size = 12.sp, color = Game.Coral, align = TextAlign.Center)
        }

        GameButton(
            text = "تایید و ورود",
            onClick = onVerifyOtp,
            modifier = Modifier.fillMaxWidth(),
            style = GameButtonStyle.Gold,
            enabled = canSubmit,
            loading = uiState.isLoading
        )

        OtpResendSection(
            countdownSeconds = uiState.countdownSeconds,
            canResendOtp = uiState.canResendOtp,
            onResendOtp = onResendOtp
        )
    }
}

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
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                // The real input stays attached (zero-size) so the keyboard and focus work.
                Box(modifier = Modifier.size(0.dp)) {
                    innerTextField()
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(boxCount) { index ->
                            val char = code.getOrNull(index)?.toString() ?: ""
                            val isFocused = code.length == index || (index == boxCount - 1 && code.length >= boxCount)
                            val borderColor = when {
                                isError -> Game.Coral
                                isFocused -> Game.Gold
                                char.isNotEmpty() -> Game.Gold.copy(alpha = 0.5f)
                                else -> Game.StrokeStrong
                            }
                            val shape = RoundedCornerShape(16.dp)
                            Box(
                                modifier = Modifier
                                    .size(width = 50.dp, height = 60.dp)
                                    .clip(shape)
                                    .background(if (isFocused) Game.PanelRaised else Game.InkSoft)
                                    .border(if (isFocused) 2.dp else 1.dp, borderColor, shape),
                                contentAlignment = Alignment.Center
                            ) {
                                GameText(
                                    text = char,
                                    size = 24.sp,
                                    bold = true,
                                    latin = true,
                                    align = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun OtpResendSection(
    countdownSeconds: Int,
    canResendOtp: Boolean,
    onResendOtp: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        if (canResendOtp) {
            GameChip(
                text = "ارسال دوباره کد",
                icon = Res.drawable.ic_refresh,
                accent = Game.Mint,
                onClick = onResendOtp
            )
        } else {
            GameText(
                text = "ارسال دوباره کد تا ${countdownSeconds.fa()} ثانیه دیگر",
                size = 12.sp,
                color = Game.TextSecondary
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
