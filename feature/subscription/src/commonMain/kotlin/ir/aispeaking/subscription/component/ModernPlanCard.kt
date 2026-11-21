package ir.aispeaking.subscription.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.SubscriptionPlan
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ui.core.loading.AppLoadingIndicator
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

private data class PlanVisualMeta(
    val accentColor: Color,
    val subtitle: String,
    val features: List<String>
)

// Common feature list derived from landing page:
// All plans offer identical features with zero differentiation and NO accent (لهجه) evaluation
private val COMMON_PLAN_FEATURES = listOf(
    "دسترسی به تمام مراحل داستانی",
    "تمرین مکالمه صوتی نامحدود با AI",
    "تصحیح در لحظه گرامر و ساختار جملات",
    "پیشنهاد هوشمند جملات حین گفتگو"
)

private fun getPlanVisualMeta(plan: SubscriptionPlan): PlanVisualMeta {
    return when {
        plan.durationDays <= 30 || plan.id.contains("1m") -> {
            PlanVisualMeta(
                accentColor = Color(0xFFFF7A45), // Warm Peach / Orange
                subtitle = "شروع سریع یادگیری و تقویت مکالمه روزمره",
                features = COMMON_PLAN_FEATURES
            )
        }
        plan.durationDays <= 90 || plan.id.contains("3m") -> {
            PlanVisualMeta(
                accentColor = Color(0xFF22C55E), // Fresh Mint / Lime Green
                subtitle = "مسیر کامل تقویت مهارت گفتاری و روان‌گویی",
                features = COMMON_PLAN_FEATURES
            )
        }
        else -> {
            PlanVisualMeta(
                accentColor = Color(0xFF0EA5E9), // Sky Blue / Cyan
                subtitle = "تسلط جامع و دسترسی نامحدود به تمامی امکانات",
                features = COMMON_PLAN_FEATURES
            )
        }
    }
}

@Composable
fun ModernPlanCard(
    plan: SubscriptionPlan,
    finalPriceTomans: Long,
    isSelected: Boolean,
    isPurchasing: Boolean,
    purchaseProgressMessage: String?,
    onPurchaseClick: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentActivePlan: Boolean = false,
    remainingDays: Int = 0
) {
    val isDark = AppTheme.isDark
    val meta = getPlanVisualMeta(plan)
    val accentColor = meta.accentColor

    val baseOriginalPrice = plan.originalPriceTomans
    val hasDiscount = baseOriginalPrice != null && baseOriginalPrice > finalPriceTomans
    val discountPercent = if (hasDiscount && baseOriginalPrice > 0) {
        (((baseOriginalPrice - finalPriceTomans) * 100) / baseOriginalPrice).toInt()
    } else plan.discountPercent

    val monthlyPrice = if (plan.durationDays >= 60) {
        finalPriceTomans / (plan.durationDays / 30).coerceAtLeast(1)
    } else null

    val cardShape = RoundedCornerShape(28.dp)

    val cardBg = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E2638).copy(alpha = 0.95f),
                Color(0xFF141A28).copy(alpha = 0.98f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.96f),
                Color(0xFFFAFBFD).copy(alpha = 0.98f)
            )
        )
    }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) accentColor.copy(alpha = 0.65f) else accentColor.copy(alpha = 0.20f),
        animationSpec = tween(300),
        label = "cardBorderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = cardShape,
                        spotColor = accentColor.copy(alpha = if (isDark) 0.40f else 0.28f),
                        ambientColor = accentColor.copy(alpha = 0.15f)
                    )
                } else {
                    Modifier.shadow(
                        elevation = 2.dp,
                        shape = cardShape,
                        spotColor = Color.Black.copy(alpha = 0.06f)
                    )
                }
            )
            .clip(cardShape)
            .background(cardBg)
            .border(
                width = if (isSelected) 1.8.dp else 1.dp,
                color = animatedBorderColor,
                shape = cardShape
            )
            .clickable(onClick = onCardClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top 3D Hero Flower Icon (compact for optimal vertical fit)
            PlanHeroIcon(
                accentColor = accentColor,
                modifier = Modifier.size(46.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Plan Badge (e.g. محبوب‌ترین or بهترین ارزش)
            val badge = plan.badge
            if (!badge.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = if (isDark) 0.25f else 0.14f))
                        .border(1.dp, accentColor.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    GameText(
                        text = badge,
                        size = 11.sp,
                        bold = true,
                        color = accentColor
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 3. Plan Title
            GameText(
                text = plan.titleFa,
                size = 19.sp,
                bold = true,
                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                align = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(3.dp))

            // 4. Plan Summary Subtitle (NO accent / لهجه mentions)
            GameText(
                text = meta.subtitle,
                size = 11.sp,
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                align = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 6.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Ornamental Divider with center glowing dot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, accentColor.copy(alpha = 0.35f))
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(accentColor.copy(alpha = 0.35f), Color.Transparent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 6. Pricing Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (hasDiscount) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        androidx.compose.material3.Text(
                            text = "${formatToPersian(baseOriginalPrice)} تومان",
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8),
                            textDecoration = TextDecoration.LineThrough,
                            fontFamily = AppTheme.typography.persianRegular
                        )
                        if (discountPercent > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(accentColor.copy(alpha = 0.16f))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                GameText(
                                    text = "${formatToPersian(discountPercent.toLong())}٪ تخفیف",
                                    size = 10.sp,
                                    bold = true,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    GameText(
                        text = formatToPersian(finalPriceTomans),
                        size = 23.sp,
                        bold = true,
                        color = if (isDark) Color.White else Color(0xFF0F172A)
                    )
                    GameText(
                        text = "تومان",
                        size = 12.sp,
                        color = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                if (monthlyPrice != null) {
                    GameText(
                        text = "معادل ماهانه ${formatToPersian(monthlyPrice)} تومان",
                        size = 11.sp,
                        color = accentColor,
                        bold = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Feature Checklist: All plans offer identical features
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                meta.features.forEach { featureText ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(17.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_done),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }

                        GameText(
                            text = featureText,
                            size = 11.5.sp,
                            color = if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B),
                            bold = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. Purchase CTA Button OR Active Subscription Days Badge
            val buttonShape = RoundedCornerShape(24.dp)

            if (isCurrentActivePlan) {
                // If user currently owns this subscription, show remaining days instead of purchase button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(buttonShape)
                        .background(
                            Brush.horizontalGradient(
                                if (isDark) listOf(Color(0xFF132F23), Color(0xFF193D2E))
                                else listOf(Color(0xFFE8F5E9), Color(0xFFC8E6C9))
                            )
                        )
                        .border(1.dp, accentColor.copy(alpha = 0.60f), buttonShape),
                    contentAlignment = Alignment.Center
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
                            text = if (remainingDays > 0) {
                                "اشتراک فعال (${formatToPersian(remainingDays.toLong())} روز باقی‌مانده)"
                            } else {
                                "اشتراک فعال"
                            },
                            color = if (isDark) Color(0xFF4ADE80) else Color(0xFF1B5E20),
                            size = 13.sp,
                            bold = true
                        )
                    }
                }
            } else {
                val buttonBg = if (isDark) {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(Color(0xFF181C24), Color(0xFF282C36))
                    )
                }

                Button(
                    onClick = onPurchaseClick,
                    enabled = !isPurchasing,
                    shape = buttonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(buttonShape)
                        .background(buttonBg)
                        .border(1.dp, accentColor.copy(alpha = 0.40f), buttonShape)
                ) {
                    if (isPurchasing && isSelected) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AppLoadingIndicator(
                                color = accentColor,
                                size = 18.dp
                            )
                            GameText(
                                text = purchaseProgressMessage ?: "در حال اتصال به درگاه...",
                                color = Color.White,
                                size = 13.sp,
                                bold = true
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_crown),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(16.dp)
                            )
                            GameText(
                                text = "خرید ${plan.titleFa}",
                                color = Color.White,
                                size = 13.5.sp,
                                bold = true
                            )
                        }
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
