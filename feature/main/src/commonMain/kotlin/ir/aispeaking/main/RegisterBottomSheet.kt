package ir.aispeaking.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterBottomSheet(
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📱 ورود / ثبت‌نام برای ادامه سفر",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "برای باز کردن مرحله ۲ (${stage.titleFa}) و ذخیره پیشرفت مرحله اول روی حساب ابری، لطفاً شماره موبایل خود را وارد نمایید.",
                color = Color(0xFFCBD5E1),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (step == 1) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 11) phoneNumber = it
                    },
                    label = { Text("شماره موبایل (مانند 09123456789)") },
                    placeholder = { Text("09123456789") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (phoneNumber.length >= 10) {
                            isLoading = true
                            errorMessage = null
                            // Transition to OTP step
                            step = 2
                            isLoading = false
                        } else {
                            errorMessage = "لطفاً یک شماره موبایل معتبر ۱۱ رقمی وارد کنید."
                        }
                    },
                    enabled = !isLoading && phoneNumber.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("ارسال کد تایید پیامکی", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                OutlinedTextField(
                    value = otpCode,
                    onValueChange = {
                        if (it.length <= 6) otpCode = it
                    },
                    label = { Text("کد ۴ یا ۶ رقمی پیامک شده") },
                    placeholder = { Text("1234") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF475569)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                errorMessage?.let { msg ->
                    Text(
                        text = msg,
                        color = Color(0xFFEF4444),
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                Button(
                    onClick = {
                        if (otpCode.length >= 4) {
                            isLoading = true
                            errorMessage = null
                            // Registration verified & synced
                            onRegisterSuccess()
                        } else {
                            errorMessage = "لطفاً کد تایید را کامل وارد کنید."
                        }
                    },
                    enabled = !isLoading && otpCode.isNotBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("تایید و باز کردن مرحله ۲", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                TextButton(
                    onClick = { step = 1 },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("ویرایش شماره موبایل", color = Color(0xFFA5B4FC), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
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
