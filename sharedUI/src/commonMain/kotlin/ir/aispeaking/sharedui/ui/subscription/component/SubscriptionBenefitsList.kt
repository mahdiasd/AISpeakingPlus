package ir.aispeaking.sharedui.ui.subscription.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_done
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private data class BenefitItem(
    val title: String,
    val icon: DrawableResource,
    val iconTint: Color,
    val containerBg: Color
)

@Composable
fun SubscriptionBenefitsList(
    modifier: Modifier = Modifier
) {
    val benefits = listOf(
        BenefitItem(
            title = "دسترسی نامحدود به تمامی مراحل سفر داستانی",
            icon = Res.drawable.ic_crown,
            iconTint = Color(0xFFFFD700),
            containerBg = Color(0x33FFD700)
        ),
        BenefitItem(
            title = "مکالمه نامحدود و بدون وقفه با هوش مصنوعی",
            icon = Res.drawable.ic_star,
            iconTint = Color(0xFF818CF8),
            containerBg = Color(0x33818CF8)
        ),
        BenefitItem(
            title = "تحلیل هوشمند جملات و فیدبک اصلاح تلفظ",
            icon = Res.drawable.ic_done,
            iconTint = Color(0xFF34D399),
            containerBg = Color(0x3334D399)
        ),
        BenefitItem(
            title = "سرعت فوق‌العاده بالا روی سرورهای اختصاصی VIP",
            icon = Res.drawable.ic_points,
            iconTint = Color(0xFFF472B6),
            containerBg = Color(0x33F472B6)
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF0F172A))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "مزایای اختصاصی اشتراک ویژه",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
        )

        benefits.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(item.containerBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = null,
                        tint = item.iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = item.title,
                    color = Color(0xFFE2E8F0),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
