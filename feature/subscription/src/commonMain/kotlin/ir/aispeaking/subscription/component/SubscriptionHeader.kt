package ir.aispeaking.subscription.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import org.jetbrains.compose.resources.painterResource

import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun SubscriptionHeader(
    currentStatus: SubscriptionStatus?,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Glowing Crown Hero Badge
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(22.dp),
                    spotColor = Color(0xFFFFC83D)
                )
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFC83D), Color(0xFFFFC83D), Color(0xFFD97706))
                    )
                )
                .border(
                    width = 2.dp,
                    color = Color(0x66FFFFFF),
                    shape = RoundedCornerShape(22.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_crown),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Main Title
        GameText(
            text = "دسترسی نامحدود به سفر داستانی",
            color = Game.TextPrimary,
            size = 22.sp,
            bold = true,
            align = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        GameText(
            text = "تمامی مراحل، سناریوهای پیشرفته و هوش مصنوعی بدون وقفه را آنلاک کنید",
            color = Game.TextSecondary,
            size = 13.sp,
            lineHeight = 22.sp,
            align = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // Active Subscription Reminder Card if user is already subscriber
        if (currentStatus?.isSubscriber == true) {
            Spacer(modifier = Modifier.height(16.dp))
            val cardBg = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFF2E1065), Color(0xFF1B2550)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)))
            }
            val cardBorder = if (isDark) {
                Brush.horizontalGradient(listOf(Color(0xFFFFC83D), Color(0xFF7C3AED)))
            } else {
                Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(cardBg)
                    .border(
                        width = 1.dp,
                        brush = cardBorder,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        GameText(
                            text = "شما دارای اشتراک فعال هستید",
                            color = if (isDark) Color(0xFFFFC83D) else Color(0xFFB45309),
                            size = 13.sp,
                            bold = true
                        )
                        GameText(
                            text = "خرید پلن جدید به انتهای روزهای باقیمانده شما اضافه خواهد شد.",
                            color = Game.TextSecondary,
                            size = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x33FFD700))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        GameText(
                            text = "${formatToPersian(currentStatus.remainingDays)} روز باقیمانده",
                            color = if (isDark) Color(0xFFFFC83D) else Color(0xFFB45309),
                            size = 12.sp,
                            bold = true
                        )
                    }
                }
            }
        }
    }
}

private fun formatToPersian(number: Int): String {
    val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    return number.toString().map { char ->
        if (char in '0'..'9') persianDigits[char - '0'] else char
    }.joinToString("")
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SubscriptionHeaderPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        SubscriptionHeader(currentStatus = null)
    }
}
