package ir.aispeaking.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText

/** Suggestion card that slides in above the input after the learner asks for a hint. */
@Composable
fun HintSuggestionCue(
    visible: Boolean,
    suggestionEn: String?,
    explanationFa: String?,
    onApplySuggestion: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && !suggestionEn.isNullOrBlank(),
        enter = slideInVertically(tween(240)) { it / 2 } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(150)),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val shape = RoundedCornerShape(22.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .background(Game.PanelSolid, shape)
                    .border(1.dp, Game.Gold.copy(alpha = 0.6f), shape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GameChip(
                        text = "جمله پیشنهادی",
                        icon = Res.drawable.ic_lamp,
                        accent = Game.Gold,
                        active = true
                    )
                    GameChip(
                        text = "بستن",
                        icon = Res.drawable.ic_close,
                        accent = Game.TextSecondary,
                        onClick = onDismiss
                    )
                }

                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    GameText(
                        text = suggestionEn ?: "",
                        size = 17.sp,
                        lineHeight = 25.sp,
                        latin = true,
                        bold = true,
                        color = Game.Sky,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (!explanationFa.isNullOrBlank()) {
                    GameText(
                        text = explanationFa,
                        size = 13.sp,
                        lineHeight = 21.sp,
                        color = Game.TextSecondary
                    )
                }

                GameChip(
                    text = "استفاده از این جمله",
                    icon = Res.drawable.ic_done,
                    accent = Game.Mint,
                    active = true,
                    onClick = { suggestionEn?.let { onApplySuggestion(it) } }
                )
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun HintSuggestionCuePreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        HintSuggestionCue(
            visible = true,
            suggestionEn = "I'd like to order a cappuccino, please.",
            explanationFa = "می‌خواهم یک کاپوچینو سفارش دهم، لطفاً.",
            onApplySuggestion = {},
            onDismiss = {}
        )
    }
}
