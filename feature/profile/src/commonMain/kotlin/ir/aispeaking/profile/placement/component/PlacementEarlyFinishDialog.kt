package ir.aispeaking.profile.placement.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlacementEarlyFinishDialog(
    visible: Boolean,
    onConfirmFinish: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val isDark = AppTheme.isDark
    val dialogBg = if (isDark) Color(0xFF1E2029) else Color(0xFFFFFFFF)
    val dialogBorder = if (isDark) Color(0x33FFFFFF) else Color(0x1F000000)

    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(dialogBg)
                    .border(1.dp, dialogBorder, RoundedCornerShape(26.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Icon Indicator
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                if (isDark) Color(0x26F59E0B) else Color(0x1DF59E0B),
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x4DF59E0B) else Color(0x33F59E0B),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_alert_circle),
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    GameText(
                        text = "اتمام آزمون تعیین سطح",
                        size = 18.sp,
                        bold = true,
                        color = if (isDark) Game.TextPrimary else Color(0xFF0F172A),
                        align = TextAlign.Center
                    )

                    GameText(
                        text = "از اینجا به بعد اگر نمی‌تونی به سوالات پاسخ بدی، با زدن این دکمه تمامی سوالات بعدی بدون نمره ثبت میشه و سطح زبانت بر اساس پاسخ‌هایی که تا به اینجا دادی محاسبه خواهد شد.",
                        size = 13.5.sp,
                        lineHeight = 23.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B),
                        align = TextAlign.Center
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GameButton(
                            text = "ثبت و محاسبه نتیجه آزمون",
                            onClick = onConfirmFinish,
                            modifier = Modifier.fillMaxWidth()
                        )

                        GameButton(
                            text = "ادامه آزمون",
                            style = GameButtonStyle.Glass,
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
