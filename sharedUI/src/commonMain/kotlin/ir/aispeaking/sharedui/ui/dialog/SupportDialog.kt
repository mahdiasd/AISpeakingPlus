package ir.aispeaking.sharedui.ui.dialog

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_rubika
import ir.aispeaking.sharedui.ic_support
import ir.aispeaking.sharedui.ic_telegram
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

const val DEFAULT_SUPPORT_TELEGRAM_URL = "https://t.me/ai_speaking_support"
const val DEFAULT_SUPPORT_RUBIKA_URL = "https://rubika.ir/ai_speaking_support"

/**
 * Support channel selection dialog allowing users to contact support
 * via Telegram or Rubika.
 */
@Composable
fun SupportDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    telegramUrl: String = DEFAULT_SUPPORT_TELEGRAM_URL,
    rubikaUrl: String = DEFAULT_SUPPORT_RUBIKA_URL,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val isDark = AppTheme.isDark

    GameModal(
        visible = visible,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Support icon container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isDark) Color(0x260A84FF) else Color(0x14007AFF))
                    .border(
                        1.dp,
                        if (isDark) Color(0x4D0A84FF) else Color(0x29007AFF),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_support),
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF0A84FF) else Color(0xFF007AFF),
                    modifier = Modifier.size(28.dp)
                )
            }

            // Title & Subtitle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameText(
                    text = "ارتباط با پشتیبانی",
                    size = 18.sp,
                    bold = true,
                    align = TextAlign.Center
                )
                GameText(
                    text = "جهت دریافت راهنمایی یا گزارش مشکل، پیام‌رسان مورد نظر خود را انتخاب کنید:",
                    size = 13.sp,
                    lineHeight = 21.sp,
                    color = Game.TextSecondary,
                    align = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Channels list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SupportChannelItem(
                    title = "تلگرام (Telegram)",
                    subtitle = "@ai_speaking_support",
                    icon = Res.drawable.ic_telegram,
                    badgeBackground = Color(0xFF2AABEE),
                    onClick = {
                        try {
                            uriHandler.openUri(telegramUrl)
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )

                SupportChannelItem(
                    title = "روبیکا (Rubika)",
                    subtitle = "@ai_speaking_support",
                    icon = Res.drawable.ic_rubika,
                    badgeBackground = if (isDark) Color(0x26FFFFFF) else Color(0x12000000),
                    onClick = {
                        try {
                            uriHandler.openUri(rubikaUrl)
                        } catch (_: Exception) {}
                        onDismiss()
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            GameButton(
                text = "بستن",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 46.dp,
                textSize = 14.sp
            )
        }
    }
}

@Composable
private fun SupportChannelItem(
    title: String,
    subtitle: String,
    icon: DrawableResource,
    badgeBackground: Color,
    onClick: () -> Unit
) {
    val isDark = AppTheme.isDark
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "support_channel_press"
    )

    val shape = RoundedCornerShape(18.dp)
    val itemBg = if (isDark) Color(0x1AFFFFFF) else Color(0x0A000000)
    val itemBorder = if (isDark) Color(0x26FFFFFF) else Color(0x14000000)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(itemBg)
            .border(1.dp, itemBorder, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(badgeBackground),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(icon),
                contentDescription = title,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            GameText(
                text = title,
                size = 15.sp,
                bold = true,
                color = Game.TextPrimary
            )
            GameText(
                text = subtitle,
                size = 12.sp,
                color = Game.TextSecondary,
                latin = true
            )
        }

        Icon(
            painter = painterResource(Res.drawable.ic_arrow_left),
            contentDescription = null,
            tint = Game.TextMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}
