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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ic_refresh
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameText

/**
 * 2026 Apple Liquid Glass Floating Suggestion Card.
 *
 * Appears floating above the bottom dock when the learner requests assistance.
 * Features:
 * - Floating visionOS frosted glass surface with specular Champagne Gold or Coral border.
 * - Hero English suggestion in Serene Cyan.
 * - Persian translation & context explanation.
 * - Tactile 1-tap apply CTA in Serene Cyan (strictly NO green).
 */
@Composable
fun HintSuggestionCue(
    visible: Boolean,
    suggestionEn: String?,
    explanationFa: String?,
    errorMessage: String? = null,
    onApplySuggestion: (String) -> Unit,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isError = !errorMessage.isNullOrBlank()
    val isSuggestion = !suggestionEn.isNullOrBlank()

    AnimatedVisibility(
        visible = visible && (isSuggestion || isError),
        enter = slideInVertically(tween(240)) { it / 2 } + fadeIn(tween(200)),
        exit = slideOutVertically(tween(180)) { it / 2 } + fadeOut(tween(150)),
        modifier = modifier
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            val shape = RoundedCornerShape(24.dp)
            val borderColor = if (isError) {
                AppleChatTheme.Coral.copy(alpha = 0.6f)
            } else {
                AppleChatTheme.Gold.copy(alpha = 0.55f)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp)
                    .clip(shape)
                    .background(AppleChatTheme.GlassPanelRaised)
                    .border(1.dp, borderColor, shape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header row: Badge + Dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GameChip(
                        text = if (isError) "خطا در راهنمایی" else "جمله پیشنهادی",
                        icon = if (isError) Res.drawable.ic_close else Res.drawable.ic_lamp,
                        accent = if (isError) AppleChatTheme.Coral else AppleChatTheme.Gold,
                        border = if (isError) AppleChatTheme.Coral.copy(alpha = 0.5f) else AppleChatTheme.Gold.copy(alpha = 0.5f),
                        active = true
                    )
                    GameChip(
                        text = "بستن",
                        icon = Res.drawable.ic_close,
                        accent = Game.TextSecondary,
                        border = AppleChatTheme.SubtleBorderColor,
                        onClick = onDismiss
                    )
                }

                if (isError) {
                    GameText(
                        text = errorMessage,
                        size = 13.sp,
                        lineHeight = 21.sp,
                        color = AppleChatTheme.Coral,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (onRetry != null) {
                        GameChip(
                            text = "تلاش مجدد",
                            icon = Res.drawable.ic_refresh,
                            accent = AppleChatTheme.Gold,
                            border = AppleChatTheme.Gold.copy(alpha = 0.5f),
                            active = true,
                            onClick = onRetry
                        )
                    }
                } else {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        GameText(
                            text = suggestionEn ?: "",
                            size = 17.sp,
                            lineHeight = 25.sp,
                            latin = true,
                            bold = true,
                            color = AppleChatTheme.SuccessCyan,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (!explanationFa.isNullOrBlank()) {
                        GameText(
                            text = explanationFa,
                            size = 13.sp,
                            lineHeight = 21.sp,
                            color = Game.TextSecondary,
                            align = TextAlign.Justify,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 1-Tap Apply CTA (Serene Cyan / Crisp Azure)
                    GameChip(
                        text = "استفاده از این جمله",
                        icon = Res.drawable.ic_done,
                        accent = AppleChatTheme.SuccessCyan,
                        border = AppleChatTheme.SuccessCyan.copy(alpha = 0.6f),
                        active = true,
                        onClick = { suggestionEn?.let { onApplySuggestion(it) } }
                    )
                }
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
