package ir.aispeaking.profile.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_exit
import org.jetbrains.compose.resources.painterResource

import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import androidx.compose.ui.text.style.TextAlign
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
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_exit),
                contentDescription = null,
                tint = Game.Coral,
                modifier = Modifier.size(40.dp)
            )
            GameText(text = "از حسابت خارج می‌شوی؟", size = 18.sp, bold = true, align = TextAlign.Center)
            GameText(
                text = "برای ورود دوباره، باید با شماره موبایل وارد شوی.",
                size = 13.sp,
                lineHeight = 21.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )
            GameButton(
                text = "بله، خارج شو",
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Danger
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
