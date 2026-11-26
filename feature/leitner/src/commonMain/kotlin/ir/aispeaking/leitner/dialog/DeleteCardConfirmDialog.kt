package ir.aispeaking.leitner.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_delete
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppleTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun DeleteCardConfirmDialog(
    card: Flashcard,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dialogShape = RoundedCornerShape(24.dp)
    val displayWord = card.headword.ifBlank { card.customWord.orEmpty() }
        .ifBlank { card.meaningFa }
        .ifBlank { "این کارت" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { if (!isDeleting) onDismiss() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .clip(dialogShape)
                .background(AppleTheme.GlassPanelRaised)
                .border(1.dp, AppleTheme.SpecularStrokeBrush, dialogShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .padding(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AppleTheme.Coral.copy(alpha = 0.16f))
                        .border(1.dp, AppleTheme.Coral.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_delete),
                        contentDescription = null,
                        tint = AppleTheme.Coral,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(
                        text = "حذف کلمه از لایتنر",
                        size = 16.sp,
                        bold = true,
                        color = Game.TextPrimary,
                        align = TextAlign.Center
                    )

                    GameText(
                        text = "آیا از حذف کلمه «$displayWord» از جعبه لایتنر مطمئن هستید؟ این عملیات قابل بازگشت نیست.",
                        size = 13.sp,
                        color = Game.TextSecondary,
                        align = TextAlign.Center
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameButton(
                        text = "انصراف",
                        style = GameButtonStyle.Glass,
                        enabled = !isDeleting,
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    )

                    GameButton(
                        text = if (isDeleting) "در حال حذف..." else "حذف کلمه",
                        icon = if (isDeleting) null else Res.drawable.ic_delete,
                        style = GameButtonStyle.Danger,
                        loading = isDeleting,
                        modifier = Modifier.weight(1.3f),
                        onClick = { if (!isDeleting) onConfirm() }
                    )
                }
            }
        }
    }
}
