package ir.aispeaking.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GameTextField

@Composable
fun RegisterBottomSheet(
    visible: Boolean = true,
    stage: Stage,
    onDismiss: () -> Unit,
    onRegisterSuccess: () -> Unit,
    isLoading: Boolean = false,
    step: Int = 1,
    errorMessage: String? = null,
    onSendOtp: ((String) -> Unit)? = null,
    onVerifyOtp: ((String, String) -> Unit)? = null,
    onEditPhone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val activeStep = step
    val activeError = errorMessage ?: localError

    GameSheet(visible = visible, onDismiss = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameText(
                text = if (activeStep == 1) "برای ادامه ماجراجویی وارد شو" else "کد تایید را وارد کن",
                size = 19.sp,
                bold = true,
                align = TextAlign.Center
            )
            GameText(
                text = if (activeStep == 1) {
                    "با ورود، مرحله «${stage.titleFa}» باز می‌شود و پیشرفتت روی حسابت ذخیره می‌ماند."
                } else {
                    "کدی که برای شماره $phoneNumber پیامک شد را وارد کن."
                },
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            if (activeStep == 1) {
                GameTextField(
                    value = phoneNumber,
                    onValueChange = { raw ->
                        val normalized = normalizeDigitsToAscii(raw).filter { c -> c in '0'..'9' }
                        if (normalized.length <= 11) {
                            phoneNumber = normalized
                            localError = null
                        }
                    },
                    label = "شماره موبایل",
                    placeholder = "09123456789",
                    keyboardType = KeyboardType.Phone,
                    ltr = true,
                    isError = activeError != null,
                    onImeAction = {}
                )
            } else {
                GameTextField(
                    value = otpCode,
                    onValueChange = { raw ->
                        val normalized = normalizeDigitsToAscii(raw).filter { c -> c in '0'..'9' }
                        if (normalized.length <= 6) {
                            otpCode = normalized
                            localError = null
                        }
                    },
                    label = "کد تایید",
                    placeholder = "••••",
                    keyboardType = KeyboardType.Number,
                    ltr = true,
                    centered = true,
                    isError = activeError != null,
                    accent = Game.Gold
                )
            }

            activeError?.let { msg ->
                GameText(text = msg, size = 12.sp, color = Game.Coral, align = TextAlign.Center)
            }

            if (activeStep == 1) {
                GameButton(
                    text = "ارسال کد تایید",
                    onClick = {
                        if (phoneNumber.length == 11 && phoneNumber.startsWith("09")) {
                            localError = null
                            onSendOtp?.invoke(phoneNumber)
                        } else {
                            localError = "شماره موبایل باید ۱۱ رقم و با ۰۹ شروع شود."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && phoneNumber.isNotBlank(),
                    loading = isLoading
                )
            } else {
                GameButton(
                    text = "تایید و باز کردن مرحله",
                    onClick = {
                        if (otpCode.length >= 4) {
                            localError = null
                            onVerifyOtp?.invoke(phoneNumber, otpCode)
                        } else {
                            localError = "کد تایید را کامل وارد کن."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = GameButtonStyle.Gold,
                    enabled = !isLoading && otpCode.isNotBlank(),
                    loading = isLoading
                )
                GameButton(
                    text = "ویرایش شماره موبایل",
                    onClick = {
                        localError = null
                        otpCode = ""
                        onEditPhone?.invoke()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = GameButtonStyle.Glass,
                    height = 44.dp,
                    textSize = 13.sp
                )
            }
            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 6.dp))
        }
    }
}

private fun normalizeDigitsToAscii(input: String): String = buildString(input.length) {
    for (ch in input) {
        when (ch) {
            in '۰'..'۹' -> append('0' + (ch - '۰'))
            in '٠'..'٩' -> append('0' + (ch - '٠'))
            else -> append(ch)
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun RegisterBottomSheetPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        RegisterBottomSheet(
            stage = Stage(
                id = "stage-2",
                orderIndex = 2,
                title = "Ordering Coffee",
                titleFa = "سفارش قهوه در کافه",
                briefing = "Order coffee at Costa",
                briefingFa = "سفارش قهوه در کافه کاستا",
                targetObjective = "Order coffee",
                targetObjectiveFa = "سفارش یک نوشیدنی گرم و پرداخت",
                backgroundUrl = "",
                characterName = "Barista Tom"
            ),
            onDismiss = {},
            onRegisterSuccess = {}
        )
    }
}
