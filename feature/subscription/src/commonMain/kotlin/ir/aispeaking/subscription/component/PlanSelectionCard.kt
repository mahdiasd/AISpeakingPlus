package ir.aispeaking.subscription.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlanSelectionCard(
    plan: SubscriptionPlan,
    finalPriceTomans: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dailyPrice = (finalPriceTomans / plan.durationDays.coerceAtLeast(1))
    val isDiscounted = finalPriceTomans < plan.priceTomans || plan.discountPercent > 0

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "border_width"
    )

    val animatedBgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF1E1B4B) else Color(0xFF1E293B),
        label = "bg_color"
    )

    val borderBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1), Color(0xFFEC4899))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color(0xFF334155), Color(0xFF1E293B))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = Color(0xFF6366F1)
                    )
                } else Modifier
            )
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = animatedBorderWidth,
                brush = borderBrush,
                shape = RoundedCornerShape(18.dp)
            )
            .background(animatedBgColor)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right Section (RTL): Radio check indicator + Title & details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Radio indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.linearGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                )
                            } else {
                                Brush.linearGradient(
                                    listOf(Color(0x33475569), Color(0x33475569))
                                )
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (isSelected) Color(0xFFA5B4FC) else Color(0xFF475569),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_done),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Plan Title + Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = plan.titleFa,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        plan.badge?.let { badgeText ->
                            val badgeGradient = if (badgeText.contains("محبوب")) {
                                listOf(Color(0xFFFFD700), Color(0xFFF59E0B))
                            } else {
                                listOf(Color(0xFF10B981), Color(0xFF059669))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.horizontalGradient(badgeGradient))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    // Duration info
                    Text(
                        text = "${formatToPersian(plan.durationDays)} روز دسترسی نامحدود",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )

                    // Daily breakdown
                    Text(
                        text = "روزی ${formatToPersian(dailyPrice)} تومان",
                        color = if (isSelected) Color(0xFFA5B4FC) else Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Left Section (RTL): Price and Discount
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Final Price
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = formatToPersian(finalPriceTomans),
                        color = if (isSelected) Color(0xFFFFD700) else Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "تومان",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                // Original crossed-out price if discounted
                if (isDiscounted && finalPriceTomans != plan.priceTomans) {
                    Text(
                        text = "${formatToPersian(plan.priceTomans)} تومان",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                // Discount percentage badge
                if (plan.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x2210B981))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${formatToPersian(plan.discountPercent)}٪ تخفیف",
                            color = Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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

private fun formatToPersian(number: Int): String {
    return formatToPersian(number.toLong())
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PlanSelectionCardPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        PlanSelectionCard(
            plan = ir.aispeaking.domain.model.stage.SubscriptionPlan(
                id = "plan-3",
                type = "3_MONTHS",
                titleFa = "اشتراک ۳ ماهه ویژه",
                durationDays = 90,
                priceTomans = 399_000L,
                discountPercent = 33,
                badge = "محبوب‌ترین"
            ),
            finalPriceTomans = 267_330L,
            isSelected = true,
            onClick = {}
        )
    }
}
