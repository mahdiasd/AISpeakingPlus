package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_microphone
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Microphone Permission Modal.
 *
 * Friendly, concise Persian copy adhering strictly to /persian-writing guidelines:
 * - Explains WHY microphone access is essential (conversational practice with AI).
 * - Explains HOW to grant access (1-tap CTA, then select Allow in system/browser prompt).
 * - Surfaces actionable troubleshooting guidance if permission is blocked/denied.
 */
@Composable
fun MicPermissionDialog(
    visible: Boolean = true,
    isPermanentlyDenied: Boolean = false,
    onGrant: () -> Unit,
    onDismiss: () -> Unit
) {
    GameModal(
        visible = visible,
        onDismiss = onDismiss,
        dismissOnScrimClick = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Luminous Circular Microphone Icon Badge
            val badgeColor = if (isPermanentlyDenied) AppleChatTheme.Coral else AppleChatTheme.PrimaryAzure
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.16f))
                    .border(1.5.dp, badgeColor.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_microphone),
                    contentDescription = null,
                    tint = badgeColor,
                    modifier = Modifier.size(34.dp)
                )
            }

            // Title
            GameText(
                text = "دسترسی به میکروفون",
                size = 20.sp,
                bold = true,
                align = TextAlign.Center
            )

            // Why section (چرا باید دسترسی داده بشه)
            GameText(
                text = "برای اینکه بتونی با هوش مصنوعی انگلیسی صحبت کنی و تلفظت سنجیده بشه، لازمه صدات رو بشنویم.",
                size = 14.sp,
                lineHeight = 22.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            // How section (چطوری دسترسی داده بشه)
            val shape = RoundedCornerShape(18.dp)
            if (isPermanentlyDenied) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(AppleChatTheme.Coral.copy(alpha = 0.08f))
                        .border(1.dp, AppleChatTheme.Coral.copy(alpha = 0.35f), shape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(
                        text = "دسترسی مسدود شده است",
                        size = 13.sp,
                        bold = true,
                        color = AppleChatTheme.Coral
                    )
                    GameText(
                        text = "لطفاً از طریق علامت قفل در کنار آدرس سایت یا تنظیمات دستگاه، دسترسی میکروفون را فعال کن و سپس دکمه زیر را بزن.",
                        size = 13.sp,
                        lineHeight = 20.sp,
                        color = Game.TextSecondary
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(shape)
                        .background(AppleChatTheme.GlassPanelRaised)
                        .border(1.dp, AppleChatTheme.SpecularBorderColor, shape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(
                        text = "چطوری فعالش کنم؟",
                        size = 13.sp,
                        bold = true,
                        color = AppleChatTheme.PrimaryAzure
                    )
                    GameText(
                        text = "۱. روی دکمه «اجازه دادن» در پایین بزن.",
                        size = 13.sp,
                        lineHeight = 20.sp,
                        color = Game.TextSecondary
                    )
                    GameText(
                        text = "۲. در پیامی که نمایش داده میشه، گزینه Allow (اجازه دادن) رو انتخاب کن.",
                        size = 13.sp,
                        lineHeight = 20.sp,
                        color = Game.TextSecondary
                    )
                }
            }

            // CTAs: Primary Allow/Retry + Secondary Later
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameButton(
                    text = "بعداً",
                    style = GameButtonStyle.Glass,
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )

                GameButton(
                    text = if (isPermanentlyDenied) "تلاش مجدد" else "اجازه دادن",
                    style = GameButtonStyle.Primary,
                    onClick = onGrant,
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }
}
