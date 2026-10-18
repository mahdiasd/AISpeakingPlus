package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Hint Confirmation Modal.
 *
 * Highlights the trade-off of using a hint (reduced stars) with high clarity:
 * - Frosted visionOS modal with Champagne Gold ambient accents.
 * - Prominent circular icon with specular light rim.
 * - Clear, persuasive typography.
 * - Primary Gold CTA + Secondary Glass CTA with spring tactile feedback.
 */
@Composable
fun HintConfirmDialog(
    visible: Boolean = true,
    hintsUsedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    GameModal(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Luminous Champagne Gold Circular Icon
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(AppleChatTheme.Gold.copy(alpha = 0.16f))
                    .border(1.5.dp, AppleChatTheme.Gold.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_lamp),
                    contentDescription = null,
                    tint = AppleChatTheme.Gold,
                    modifier = Modifier.size(34.dp)
                )
            }

            GameText(
                text = "راهنما می‌خوای؟",
                size = 20.sp,
                bold = true,
                align = TextAlign.Center
            )

            GameText(
                text = "یک جمله مناسب برای ادامه گفتگو بهت پیشنهاد می‌دهیم تا گیر نکنی.",
                size = 14.sp,
                lineHeight = 22.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            // Warning / Star Penalty Guidance Card
            val shape = RoundedCornerShape(18.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(AppleChatTheme.Gold.copy(alpha = 0.08f))
                    .border(1.dp, AppleChatTheme.Gold.copy(alpha = 0.35f), shape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                GameText(
                    text = "نکته مهم",
                    size = 13.sp,
                    bold = true,
                    color = AppleChatTheme.Gold
                )
                GameText(
                    text = "هر بار گرفتن راهنما از ستاره‌های این مرحله کم می‌کند. برای گرفتن ۳ ستاره، بدون راهنما و بدون خطا جلو برو.",
                    size = 13.sp,
                    lineHeight = 21.sp,
                    color = Game.TextPrimary
                )
                if (hintsUsedCount > 0) {
                    GameText(
                        text = "تا الان ${hintsUsedCount.fa()} بار راهنما گرفته‌ای.",
                        size = 12.sp,
                        color = AppleChatTheme.SuccessCyan
                    )
                }
            }

            // CTAs
            GameButton(
                text = "بله، راهنما بده",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Gold,
                height = 48.dp
            )

            GameButton(
                text = "فعلاً نه",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 44.dp,
                textSize = 14.sp
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun HintConfirmDialogPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        HintConfirmDialog(
            hintsUsedCount = 1,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
