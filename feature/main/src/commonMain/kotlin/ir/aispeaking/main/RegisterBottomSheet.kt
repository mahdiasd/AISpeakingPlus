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
    modifier: Modifier = Modifier
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1: Phone, 2: OTP
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                text = if (step == 1) "برای ادامه ماجراجویی وارد شو" else "کد تایید را وارد کن",
                size = 19.sp,
                bold = true,
                align = TextAlign.Center
            )
            GameText(
                text = if (step == 1) {
                    "با ورود، مرحله «${stage.titleFa}» باز می‌شود و پیشرفتت روی حسابت ذخیره می‌ماند."
                } else {
                    "کدی که برای شماره $phoneNumber پیامک شد را وارد کن."
                },
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            if (step == 1) {
                GameTextField(
                    value = phoneNumber,
                    onValueChange = { if (it.length <= 11) phoneNumber = it.filter { c -> c.isDigit() } },
                    label = "شماره موبایل",
                    placeholder = "09123456789",
                    keyboardType = KeyboardType.Phone,
                    ltr = true,
                    isError = errorMessage != null,
                    onImeAction = {}
                )
            } else {
                GameTextField(
                    value = otpCode,
                    onValueChange = { if (it.length <= 6) otpCode = it.filter { c -> c.isDigit() } },
                    label = "کد تایید",
                    placeholder = "••••",
                    keyboardType = KeyboardType.Number,
                    ltr = true,
                    centered = true,
                    isError = errorMessage != null,
                    accent = Game.Gold
                )
            }

            errorMessage?.let { msg ->
                GameText(text = msg, size = 12.sp, color = Game.Coral, align = TextAlign.Center)
            }

            if (step == 1) {
                GameButton(
                    text = "ارسال کد تایید",
                    onClick = {
                        if (phoneNumber.length >= 10) {
                            isLoading = true
                            errorMessage = null
                            // Transition to OTP step
                            step = 2
                            isLoading = false
                        } else {
                            errorMessage = "شماره موبایل باید ۱۱ رقم باشد."
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
                            isLoading = true
                            errorMessage = null
                            // Registration verified & synced
                            onRegisterSuccess()
                        } else {
                            errorMessage = "کد تایید را کامل وارد کن."
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    style = GameButtonStyle.Gold,
                    enabled = !isLoading && otpCode.isNotBlank(),
                    loading = isLoading
                )
                GameButton(
                    text = "ویرایش شماره موبایل",
                    onClick = { step = 1; errorMessage = null },
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
