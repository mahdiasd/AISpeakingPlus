package ir.aispeaking.profile.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.SubscriptionSummary
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.game.toFaDigits
import org.jetbrains.compose.resources.painterResource

@Composable
fun SubscriptionCard(
    subscription: SubscriptionSummary,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)

    if (subscription.isSubscriber) {
        // Apple VIP / Luxury Member Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF221F2B),
                            Color(0xFF16151E),
                            Game.PanelSolid
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Game.Gold.copy(alpha = 0.55f),
                            Game.Violet.copy(alpha = 0.35f),
                            Game.Gold.copy(alpha = 0.35f)
                        )
                    ),
                    shape = shape
                )
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Golden Crown Squircle
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Game.Gold.copy(alpha = 0.16f))
                                .border(1.dp, Game.Gold.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_crown),
                                contentDescription = null,
                                tint = Game.Gold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column {
                            GameText(
                                text = subscription.planTitleFa ?: "اشتراک طلایی VIP",
                                color = Game.TextPrimary,
                                size = 16.sp,
                                bold = true
                            )
                            val expiresAt = subscription.expiresAt
                            if (!expiresAt.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                GameText(
                                    text = "اعتبار تا ${expiresAt.take(10).toFaDigits()}",
                                    color = Game.TextSecondary,
                                    size = 12.sp
                                )
                            }
                        }
                    }

                    // Remaining days badge and tactile extend button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x22000000))
                                .border(1.dp, Game.Gold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            GameText(
                                text = "${subscription.remainingDays.fa()} روز مانده",
                                color = Game.Gold,
                                size = 12.sp,
                                bold = true
                            )
                        }

                        // Tactile Extend action
                        val extendInteraction = remember { MutableInteractionSource() }
                        val extendPressed by extendInteraction.collectIsPressedAsState()
                        val extendScale by animateFloatAsState(
                            targetValue = if (extendPressed) 0.94f else 1f,
                            animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                            label = "apple_extend_press"
                        )
                        Box(
                            modifier = Modifier
                                .graphicsLayer {
                                    scaleX = extendScale
                                    scaleY = extendScale
                                }
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x26FFFFFF))
                                .border(1.dp, Game.StrokeStrong, RoundedCornerShape(12.dp))
                                .clickable(
                                    interactionSource = extendInteraction,
                                    indication = null,
                                    onClick = onUpgradeClick
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GameText(
                                text = "تمدید",
                                color = Game.TextPrimary,
                                size = 12.sp,
                                bold = true
                            )
                        }
                    }
                }

                // Expiring warning alert banner
                if (subscription.isExpiringSoon) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Game.Coral.copy(alpha = 0.12f))
                            .border(1.dp, Game.Coral.copy(alpha = 0.32f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            GameText(
                                text = "اشتراک شما رو به پایان است!",
                                color = Game.Coral,
                                size = 12.sp,
                                bold = true
                            )

                            GameText(
                                text = "تمدید سریع ←",
                                color = Game.Gold,
                                size = 12.sp,
                                bold = true,
                                modifier = Modifier.clickable(onClick = onUpgradeClick)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Apple Upgrade / Free Plan Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Game.PanelRaised,
                            Game.PanelSolid
                        )
                    )
                )
                .border(1.dp, Game.StrokeStrong, shape)
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x1AFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_crown),
                                contentDescription = null,
                                tint = Game.TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        GameText(
                            text = "طرح رایگان",
                            color = Game.TextPrimary,
                            size = 15.sp,
                            bold = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    GameText(
                        text = "دسترسی نامحدود به تمامی مراحل، سناریوها و مکالمه هوش مصنوعی",
                        color = Game.TextSecondary,
                        size = 12.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                GameButton(
                    text = "خرید اشتراک",
                    onClick = onUpgradeClick,
                    style = GameButtonStyle.Gold,
                    height = 42.dp,
                    textSize = 13.sp
                )
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SubscriptionCardPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        SubscriptionCard(
            subscription = SubscriptionSummary(
                isSubscriber = true,
                planTitleFa = "اشتراک طلایی",
                remainingDays = 12,
                expiresAt = "2026-11-20"
            ),
            onUpgradeClick = {}
        )
    }
}
