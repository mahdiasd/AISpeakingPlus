package ir.aispeaking.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_exit
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import org.jetbrains.compose.resources.painterResource

@Composable
fun SignOutConfirmDialog(
    visible: Boolean = true,
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
            // Apple Destructive Icon Container
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Game.Coral.copy(alpha = 0.14f))
                    .border(1.dp, Game.Coral.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_exit),
                    contentDescription = null,
                    tint = Game.Coral,
                    modifier = Modifier.size(26.dp)
                )
            }

            GameText(
                text = "خروج از حساب کاربری؟",
                size = 18.sp,
                bold = true,
                align = TextAlign.Center
            )

            GameText(
                text = "با خروج از حساب، برای دسترسی مجدد باید با شماره موبایل خود وارد شوید.",
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            GameButton(
                text = "بله، خارج شو",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Danger,
                height = 48.dp
            )

            GameButton(
                text = "انصراف",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 46.dp,
                textSize = 14.sp
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun SignOutConfirmDialogPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        SignOutConfirmDialog(
            onConfirm = {},
            onDismiss = {}
        )
    }
}
