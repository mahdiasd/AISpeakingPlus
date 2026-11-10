package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import ir.aispeaking.sharedui.ic_alert_circle
import ir.aispeaking.sharedui.ic_exit
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass Exit / Finish Confirmation Modal.
 *
 * Ensures learner is never abruptly exited from conversation on back navigation or finish CTA.
 * Protects learner's time and progress with clear choices and empathetic copy.
 */
@Composable
fun ChatExitConfirmDialog(
    visible: Boolean,
    isFinishAction: Boolean,
    turnsCount: Int,
    onConfirmFinish: () -> Unit,
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val hasTurns = turnsCount > 0

    GameModal(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Luminous halo icon matching action context
            val iconRes = when {
                isFinishAction && hasTurns -> Res.drawable.ic_star
                hasTurns -> Res.drawable.ic_alert_circle
                else -> Res.drawable.ic_exit
            }
            val accentColor = when {
                isFinishAction && hasTurns -> AppleChatTheme.Gold
                hasTurns -> AppleChatTheme.Coral
                else -> AppleChatTheme.PrimaryAzure
            }

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(1.5.dp, accentColor.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Title
            val titleText = when {
                isFinishAction && hasTurns -> "پایان گفتگو"
                hasTurns -> "خروج از گفتگو"
                else -> "خروج از مرحله"
            }
            GameText(
                text = titleText,
                size = 20.sp,
                bold = true,
                align = TextAlign.Center
            )

            // Description
            val descriptionText = when {
                isFinishAction && hasTurns ->
                    "آیا از پایان دادن به این گفتگو اطمینان دارید؟ با تایید، عملکرد شما بررسی شده و کارنامه مرحله صادر می‌شود."
                hasTurns ->
                    "در صورت خروج مستقیم، پیشرفت و پیام‌های این مرحله ذخیره نمی‌شوند. مایلید مکالمه را ثبت و ارزیابی کنید یا بدون ذخیره خارج شوید؟"
                else ->
                    "شما هنوز پیامی ارسال نکرده‌اید. آیا مایلید از این مرحله خارج شوید؟"
            }
            GameText(
                text = descriptionText,
                size = 14.sp,
                lineHeight = 22.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (isFinishAction && hasTurns) {
                    GameButton(
                        text = "پایان و ارزیابی مرحله",
                        onClick = onConfirmFinish,
                        style = GameButtonStyle.Gold,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp,
                        textSize = 14.sp
                    )
                    GameButton(
                        text = "ادامه گفتگو",
                        onClick = onDismiss,
                        style = GameButtonStyle.Glass,
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        textSize = 13.sp
                    )
                } else if (hasTurns) {
                    GameButton(
                        text = "پایان و ثبت امتیاز",
                        onClick = onConfirmFinish,
                        style = GameButtonStyle.Gold,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp,
                        textSize = 14.sp
                    )
                    GameButton(
                        text = "خروج بدون ذخیره",
                        onClick = onConfirmExit,
                        style = GameButtonStyle.Danger,
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        textSize = 13.sp
                    )
                    GameButton(
                        text = "ادامه گفتگو",
                        onClick = onDismiss,
                        style = GameButtonStyle.Glass,
                        modifier = Modifier.fillMaxWidth(),
                        height = 42.dp,
                        textSize = 13.sp
                    )
                } else {
                    GameButton(
                        text = "خروج از مرحله",
                        onClick = onConfirmExit,
                        style = GameButtonStyle.Danger,
                        modifier = Modifier.fillMaxWidth(),
                        height = 46.dp,
                        textSize = 14.sp
                    )
                    GameButton(
                        text = "ادامه گفتگو",
                        onClick = onDismiss,
                        style = GameButtonStyle.Primary,
                        modifier = Modifier.fillMaxWidth(),
                        height = 44.dp,
                        textSize = 13.sp
                    )
                }
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatExitConfirmDialogPreview() {
    AppTheme {
        ChatExitConfirmDialog(
            visible = true,
            isFinishAction = false,
            turnsCount = 3,
            onConfirmFinish = {},
            onConfirmExit = {},
            onDismiss = {}
        )
    }
}
