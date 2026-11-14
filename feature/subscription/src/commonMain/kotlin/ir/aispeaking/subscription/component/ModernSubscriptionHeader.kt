package ir.aispeaking.subscription.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.SubscriptionStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_scenario
import ir.aispeaking.sharedui.ic_trend_up
import ir.aispeaking.sharedui.ic_voice
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class HeaderFeaturePill(
    val title: String,
    val icon: DrawableResource
)

@Composable
fun ModernSubscriptionHeader(
    currentStatus: SubscriptionStatus?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark

    val featurePills = listOf(
        HeaderFeaturePill("مکالمه صوتی", Res.drawable.ic_voice),
        HeaderFeaturePill("مراحل داستانی", Res.drawable.ic_scenario),
        HeaderFeaturePill("ارزیابی لهجه", Res.drawable.ic_trend_up)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Bar: Back button on the right (RTL start), balanced with spacer on left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppBackButton(
                onClick = onNavigateBack,
                containerSize = 38.dp,
                iconSize = 18.dp
            )

            // Small subtle pill badge: { تعرفه‌ها } matching ( Pricing ) in image
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isDark) Color(0x33FFFFFF) else Color(0x14000000)
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0x22FFFFFF) else Color(0x12000000),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                GameText(
                    text = "{ تعرفه‌های اشتراک }",
                    size = 11.sp,
                    color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                    bold = true
                )
            }

            // Empty spacer to balance the back button
            Spacer(modifier = Modifier.size(38.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Main Title
        GameText(
            text = "دسترسی نامحدود با هر اشتراک",
            size = 19.sp,
            bold = true,
            color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            align = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Feature Pills Row (matching the image design)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            featurePills.forEach { item ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isDark) Color(0x2EFFFFFF) else Color(0x12000000)
                            )
                            .border(
                                1.dp,
                                if (isDark) Color(0x22FFFFFF) else Color(0x15000000),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(item.icon),
                            contentDescription = null,
                            tint = if (isDark) Color(0xFFE2E8F0) else Color(0xFF334155),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    GameText(
                        text = item.title,
                        size = 11.sp,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        bold = false
                    )
                }
            }
        }

        // Active subscription reminder banner if user is currently subscribed
        if (currentStatus?.isSubscriber == true) {
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isDark) {
                            Brush.horizontalGradient(listOf(Color(0xFF2E1065), Color(0xFF1B2550)))
                        } else {
                            Brush.horizontalGradient(listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7)))
                        }
                    )
                    .border(
                        1.dp,
                        if (isDark) Color(0xFF7C3AED) else Color(0xFFFCD34D),
                        RoundedCornerShape(14.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_crown),
                            contentDescription = null,
                            tint = Color(0xFFFFC83D),
                            modifier = Modifier.size(18.dp)
                        )
                        GameText(
                            text = "اشتراک شما فعال است",
                            size = 12.sp,
                            bold = true,
                            color = if (isDark) Color(0xFFDDD6FE) else Color(0xFF92400E)
                        )
                    }

                    GameText(
                        text = "${formatToPersian(currentStatus.remainingDays.toLong())} روز باقی‌مانده",
                        size = 11.sp,
                        bold = true,
                        color = if (isDark) Color(0xFFA78BFA) else Color(0xFFB45309)
                    )
                }
            }
        }
    }
}

private fun formatToPersian(number: Long): String {
    val persianDigits = listOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
    val formatted = number.toString().reversed().chunked(3).joinToString(",").reversed()
    return formatted.map { char ->
        if (char in '0'..'9') persianDigits[char - '0'] else char
    }.joinToString("")
}
