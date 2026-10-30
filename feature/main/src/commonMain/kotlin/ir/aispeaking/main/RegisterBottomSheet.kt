package ir.aispeaking.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.ui.dialog.RegisterBottomSheet as SharedRegisterBottomSheet

@Composable
fun RegisterBottomSheet(
    visible: Boolean = true,
    stage: Stage? = null,
    title: String = "برای ادامه ماجراجویی وارد شو",
    subtitle: String? = null,
    confirmButtonText: String? = null,
    onDismiss: () -> Unit,
    onRegisterSuccess: () -> Unit = {},
    isLoading: Boolean = false,
    step: Int = 1,
    errorMessage: String? = null,
    onSendOtp: ((String) -> Unit)? = null,
    onVerifyOtp: ((String, String) -> Unit)? = null,
    onEditPhone: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    SharedRegisterBottomSheet(
        visible = visible,
        stage = stage,
        title = title,
        subtitle = subtitle,
        confirmButtonText = confirmButtonText,
        onDismiss = onDismiss,
        onRegisterSuccess = onRegisterSuccess,
        isLoading = isLoading,
        step = step,
        errorMessage = errorMessage,
        onSendOtp = onSendOtp,
        onVerifyOtp = onVerifyOtp,
        onEditPhone = onEditPhone,
        modifier = modifier
    )
}
