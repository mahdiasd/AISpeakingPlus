package ir.aispeaking.feature.auth.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun PrivacyPolicyDialog(
    show: Boolean,
    onDismiss: () -> Unit,
) {
    if (show) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false) // Full screen
        ) {
            PrivacyPolicyDialogContent(onDismiss = onDismiss)
        }
    }
}

@Composable
fun PrivacyPolicyDialogContent(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(32.dp)
                    .background(AppTheme.colors.primaryContainer, shape = CircleShape)
                    .padding(4.dp)
                    .animateClickable(onDismiss),
                tint = AppTheme.colors.onPrimaryContainer,
                painter = painterResource(Res.drawable.ic_close),
                contentDescription = "Close dialog",
            )
            TitleBoldText(
                text = "سیاست حفظ حریم خصوصی",
                persianFont = true,
                color = AppTheme.colors.primary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        BodyMediumText(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Justify,
            persianFont = true,
            text = """
                ما به حریم خصوصی کاربران خود احترام می‌گذاریم و متعهد هستیم که اطلاعات شخصی شما را تنها در حد نیاز برای ارائه خدمات بهتر جمع‌آوری و استفاده کنیم. استفاده شما از این اپلیکیشن به معنای پذیرش این سیاست است.

                ۱. اطلاعاتی که جمع‌آوری می‌کنیم:
                - نام، شماره تلفن و نام نمایشی (Nickname).
                - مدل گوشی، نسخه اندروید.
                - تعداد سناریوها و چالش‌های انجام شده و امتیازهای کسب شده.

                ۲. اطلاعاتی که جمع‌آوری نمی‌کنیم:
                - محتوای صوتی بلافاصله پس از تبدیل به متن حذف می‌شود و ذخیره نمی‌گردد.
                - سناریوها و پیام‌های رد و بدل شده ذخیره نمی‌شوند.

                ۳. نحوه استفاده از اطلاعات:
                - ارائه و بهبود تجربه آموزشی.
                - ثبت و نمایش امتیازها و پیشرفت‌ها.

                ۴. امنیت:
                - داده‌ها روی سرورهای امن ذخیره می‌شود و فقط کارکنان مجاز دسترسی دارند.

                ۵. اشتراک‌گذاری:
                - فقط با رضایت شما یا براساس قوانین کشور.

                ۶. حقوق کاربر:
                - دسترسی، ویرایش یا حذف اطلاعات شخصی.

                تاریخ به‌روزرسانی: ۱۴۰۴/۰۵/۱۵
                """.trimIndent(),
        )

    }
}

@LightDarkPreview
@Composable
fun PrivacyPolicyDialogPreview() {
    AppTheme {
        PrivacyPolicyDialogContent(onDismiss = {})
    }
}
