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
    val isDiscounted = finalPriceTomans < plan.priceTomans || plan.discountPercent > 0

    val animatedBorderWidth by animateDpAsState(
        targetValue = if (isSelected) 2.dp else 1.dp,
        label = "border_width"
    )

    val targetBgColor = if (isDark) {
        if (isSelected) Game.PanelRaised else Game.PanelSolid
    } else {
        if (isSelected) Color(0xFFF0F6FF) else Color(0xFFFFFFFF)
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
            .then(
                if (isSelected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(18.dp),
                        spotColor = if (isDark) Color(0xFF5E5CE6) else Color(0x33007AFF)
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
                        GameText(
                            text = plan.titleFa,
                            color = Game.TextPrimary,
                            size = 16.sp,
                            bold = true
                        )

                        plan.badge?.let { badgeText ->
                            val badgeGradient = if (badgeText.contains("محبوب")) {
                                listOf(Color(0xFFFFD60A), Color(0xFFFF9500))
                            } else {
                                listOf(Color(0xFF007AFF), Color(0xFF5856D6))
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.horizontalGradient(badgeGradient))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                GameText(
                                    text = badgeText,
                                    color = if (badgeText.contains("محبوب")) Color.Black else Color.White,
                                    size = 11.sp,
                                    bold = true
                                )
                            }
                        }
                    }

                    // Duration info
                    GameText(
                        text = "${formatToPersian(plan.durationDays)} روز دسترسی نامحدود",
                        color = Game.TextSecondary,
                        size = 12.sp
                    )

                    // Daily breakdown
                    GameText(
                        text = "روزی ${formatToPersian(dailyPrice)} تومان",
                        color = Game.TextMuted,
                        size = 11.sp
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
                    GameText(
                        text = formatToPersian(finalPriceTomans),
                        color = if (isSelected) (if (isDark) Color(0xFFFFD60A) else Color(0xFF007AFF)) else Game.TextPrimary,
                        size = 17.sp,
                        bold = true
                    )
                    GameText(
                        text = "تومان",
                        color = Game.TextSecondary,
                        size = 11.sp
                    )
                }

                // Original crossed-out price if discounted
                if (isDiscounted && finalPriceTomans != plan.priceTomans) {
                    Text(
                        text = "${formatToPersian(plan.priceTomans)} تومان",
                        color = Game.TextMuted,
                        fontSize = 11.sp,
                        fontFamily = AppTheme.typography.persianRegular,
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                // Discount percentage badge
                if (plan.discountPercent > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF007AFF).copy(alpha = if (isDark) 0.22f else 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        GameText(
                            text = "${formatToPersian(plan.discountPercent)}٪ تخفیف",
                            color = Color(0xFF007AFF),
                            size = 11.sp,
                            bold = true
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
