package ir.aispeaking.profile.placement.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun PlacementExitConfirmDialog(
    visible: Boolean,
    onConfirmExit: () -> Unit,
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
                    GameText(
                        text = "خروج از آزمون تعیین سطح؟",
                        size = 17.sp,
                        bold = true,
                        color = if (isDark) Game.TextPrimary else Color(0xFF0F172A),
                        align = TextAlign.Center
                    )

                    GameText(
                        text = "در صورت خروج، پاسخ‌های ثبت‌شده ذخیره نمی‌شوند و آزمون لغو خواهد شد.",
                        size = 13.sp,
                        lineHeight = 22.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B),
                        align = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GameButton(
                            text = "ادامه آزمون",
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        )

                        GameButton(
                            text = "خروج",
                            style = GameButtonStyle.Glass,
                            onClick = onConfirmExit,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
