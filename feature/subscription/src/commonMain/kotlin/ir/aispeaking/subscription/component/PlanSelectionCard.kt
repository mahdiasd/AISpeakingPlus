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

import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun PlanSelectionCard(
    plan: SubscriptionPlan,
    finalPriceTomans: Long,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val dailyPrice = (finalPriceTomans / plan.durationDays.coerceAtLeast(1))
    val monthlyPrice = if (plan.durationDays >= 60) {
        finalPriceTomans / (plan.durationDays / 30).coerceAtLeast(1)
    } else null
    val baseOriginalPrice = plan.originalPriceTomans
    val hasBaseDiscount = baseOriginalPrice != null && baseOriginalPrice > plan.priceTomans
    val isDiscounted = finalPriceTomans < plan.priceTomans || plan.discountPercent > 0 || hasBaseDiscount

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "border_width"
    )

    val targetBgColor = if (isDark) {
        if (isSelected) Game.PanelRaised else Game.PanelSolid
    } else {
        if (isSelected) Color(0xD9EBF5FF) else Game.PanelSolid
    }

    val animatedBgColor by animateColorAsState(
        targetValue = targetBgColor,
        label = "bg_color"
    )

    val borderBrush = if (isSelected) {
        Brush.horizontalGradient(
            colors = listOf(Color(0xFF007AFF), Color(0xFF5E5CE6))
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Game.Stroke, Game.Stroke)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (plan.badge != null) 7.dp else 0.dp)
    ) {
        // Card Main Body
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isSelected) {
                        Modifier.shadow(
                            elevation = 6.dp,
                            shape = RoundedCornerShape(16.dp),
                            spotColor = if (isDark) Color(0xFF5E5CE6) else Color(0x33007AFF)
                        )
                    } else Modifier
                )
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = animatedBorderWidth,
                    brush = borderBrush,
                    shape = RoundedCornerShape(16.dp)
                )
                .background(animatedBgColor)
                .clickable { onClick() }
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Right Section (RTL): Radio check indicator + Title & details
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    // Radio indicator
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    Brush.linearGradient(
                                        listOf(Color(0xFF007AFF), Color(0xFF5E5CE6))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        listOf(Game.Panel, Game.Panel)
                                    )
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isSelected) Color(0xFF007AFF) else Game.StrokeStrong,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_done),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        // Plan Title (Clean, unconstrained, badge pinned to card top)
                        GameText(
                            text = plan.titleFa,
                            color = Game.TextPrimary,
                            size = 15.sp,
                            bold = true,
                            maxLines = 1
                        )

                        // Duration info
                        GameText(
                            text = "${formatToPersian(plan.durationDays)} روز دسترسی نامحدود",
                            color = Game.TextSecondary,
                            size = 11.sp,
                            maxLines = 1
                        )

                        // Monthly / Daily breakdown
                        if (monthlyPrice != null) {
                            GameText(
                                text = "ماهی فقط ${formatToPersian(monthlyPrice)} تومان",
                                color = if (isSelected) (if (isDark) Color(0xFFFFD60A) else Color(0xFF007AFF)) else Game.TextSecondary,
                                size = 11.sp,
                                bold = true,
                                maxLines = 1
                            )
                        } else {
                            GameText(
                                text = "روزی ${formatToPersian(dailyPrice)} تومان",
                                color = Game.TextMuted,
                                size = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Left Section (RTL): Price and Discount
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Final Price
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        GameText(
                            text = formatToPersian(finalPriceTomans),
                            color = if (isSelected) (if (isDark) Color(0xFFFFD60A) else Color(0xFF007AFF)) else Game.TextPrimary,
                            size = 16.sp,
                            bold = true,
                            maxLines = 1
                        )
                        GameText(
                            text = "تومان",
                            color = Game.TextSecondary,
                            size = 10.sp,
                            maxLines = 1
                        )
                    }

                    // Original crossed-out price if discounted
                    val crossedOutPrice = when {
                        finalPriceTomans != plan.priceTomans -> plan.priceTomans
                        hasBaseDiscount -> baseOriginalPrice
                        else -> null
                    }
                    if (crossedOutPrice != null) {
                        Text(
                            text = "${formatToPersian(crossedOutPrice)} تومان",
                            color = Game.TextMuted,
                            fontSize = 10.sp,
                            fontFamily = AppTheme.typography.persianRegular,
                            textDecoration = TextDecoration.LineThrough,
                            maxLines = 1
                        )
                    }

                    // Discount percentage badge (Emerald savings incentive)
                    if (plan.discountPercent > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color(0xFF34C759).copy(alpha = if (isDark) 0.22f else 0.14f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            GameText(
                                text = "${formatToPersian(plan.discountPercent)}٪ تخفیف",
                                color = if (isDark) Color(0xFF30D158) else Color(0xFF248A3D),
                                size = 10.sp,
                                bold = true,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Pinned Top Badge: Attached directly to the card's top edge (e.g. «محبوب‌ترین» or «بهترین ارزش»)
        plan.badge?.let { badgeText ->
            val isPopular = badgeText.contains("محبوب")
            val badgeGradient = if (isPopular) {
                listOf(Color(0xFFFFD60A), Color(0xFFFF9500))
            } else {
                listOf(Color(0xFF007AFF), Color(0xFF5E5CE6))
            }
            val textColor = if (isPopular) Color(0xFF1C1917) else Color.White

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 22.dp)
                    .offset(y = (-7).dp)
                    .shadow(elevation = 3.dp, shape = RoundedCornerShape(10.dp))
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.horizontalGradient(badgeGradient))
                    .padding(horizontal = 9.dp, vertical = 2.dp)
            ) {
                GameText(
                    text = badgeText,
                    color = textColor,
                    size = 10.sp,
                    bold = true,
                    maxLines = 1
                )
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
