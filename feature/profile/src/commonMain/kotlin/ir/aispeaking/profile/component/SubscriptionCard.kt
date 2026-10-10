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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.user.SubscriptionSummary
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_alert_circle
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
    val shape = RoundedCornerShape(24.dp)

    if (subscription.isSubscriber) {
        // Active / Trial Subscription Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color(0xFF1B1C23))
                .border(1.dp, Color(0x22FFFFFF), shape)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Row: Crown + Plan info (Right) and "تمدید" button (Left) in RTL
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right side in RTL: Crown squircle + Plan Details
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Golden Crown Squircle
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(Color(0x26FFD60A))
                                .border(1.dp, Color(0x4DFFD60A), RoundedCornerShape(13.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_crown),
                                contentDescription = null,
                                tint = Game.Gold,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                GameText(
                                    text = subscription.planTitleFa?.ifBlank { "TRIAL" } ?: "TRIAL",
                                    color = Game.Gold,
                                    size = 17.sp,
                                    bold = true
                                )

                                // Remaining Days Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x33FFD60A))
                                        .border(1.dp, Color(0x4DFFD60A), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    GameText(
                                        text = "${subscription.remainingDays.fa()} روز مانده",
                                        color = Game.Gold,
                                        size = 11.sp,
                                        bold = true
                                    )
                                }
                            }

                            val expiresAt = subscription.expiresAt
                            if (!expiresAt.isNullOrBlank()) {
                                GameText(
                                    text = "اعتبار تا ${expiresAt.take(10).toFaDigits()}",
                                    color = Game.TextSecondary,
                                    size = 11.sp
                                )
                            }
                        }
                    }

                    // Left side in RTL: Tactile "تمدید" action button
                    val extendInteraction = remember { MutableInteractionSource() }
                    val extendPressed by extendInteraction.collectIsPressedAsState()
                    val extendScale by animateFloatAsState(
                        targetValue = if (extendPressed) 0.94f else 1f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                        label = "extend_press"
                    )

                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = extendScale
                                scaleY = extendScale
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22FFFFFF))
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = extendInteraction,
                                indication = null,
                                onClick = onUpgradeClick
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp),
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

                // Expiring warning alert banner with clean, symmetric margins & padding
                if (subscription.isExpiringSoon) {
                    Spacer(modifier = Modifier.height(14.dp))

                    val quickExtendInteraction = remember { MutableInteractionSource() }
                    val quickExtendPressed by quickExtendInteraction.collectIsPressedAsState()
                    val quickExtendScale by animateFloatAsState(
                        targetValue = if (quickExtendPressed) 0.95f else 1f,
                        animationSpec = spring(dampingRatio = 0.82f, stiffness = 420f),
                        label = "quick_extend_press"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x26FF453A))
                            .border(1.dp, Color(0x40FF453A), RoundedCornerShape(14.dp))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Right side in RTL: Alert circle icon + warning text
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_alert_circle),
                                    contentDescription = null,
                                    tint = Game.Coral,
                                    modifier = Modifier.size(17.dp)
                                )

                                GameText(
                                    text = "اشتراک شما رو به پایان است!",
                                    color = Game.Coral,
                                    size = 12.sp,
                                    bold = true
                                )
                            }

                            // Left side in RTL: "تمدید سریع" button
                            Box(
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = quickExtendScale
                                        scaleY = quickExtendScale
                                    }
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0x40FF453A))
                                    .border(1.dp, Color(0x55FF453A), RoundedCornerShape(10.dp))
                                    .clickable(
                                        interactionSource = quickExtendInteraction,
                                        indication = null,
                                        onClick = onUpgradeClick
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                GameText(
                                    text = "تمدید سریع",
                                    color = Color(0xFFFFD4D1),
                                    size = 11.sp,
                                    bold = true
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Free Plan Card
        Box(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(Color(0xFF1B1C23))
                .border(1.dp, Color(0x22FFFFFF), shape)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(11.dp))
                                .background(Color(0x1AFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_crown),
                                contentDescription = null,
                                tint = Game.TextSecondary,
                                modifier = Modifier.size(19.dp)
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
                        size = 11.sp,
                        lineHeight = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                GameButton(
                    text = "خرید اشتراک",
                    onClick = onUpgradeClick,
                    style = GameButtonStyle.Gold,
                    height = 38.dp,
                    textSize = 12.sp
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
                planTitleFa = "TRIAL",
                remainingDays = 5,
                expiresAt = "2026-10-15"
            ),
            onUpgradeClick = {}
        )
    }
}
