package ir.aispeaking.chat.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameProgressBar
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme

/**
 * HUD at the top of a conversation. First row: back / who you are talking to / finish.
 * Second row: the goal chip and a turn counter with progress, so the learner always knows
 * what to do and how far along they are.
 */
@Composable
fun ChatToolbar(
    modifier: Modifier = Modifier,
    stage: Stage?,
    turnsCount: Int = 0,
    isFinishing: Boolean = false,
    onBackClick: () -> Unit,
    onObjectiveClick: () -> Unit = {},
    onFinishConversationClick: () -> Unit
) {
    val maxTurns = (stage?.maxTurns ?: 0).coerceAtLeast(1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameIconButton(
                icon = Res.drawable.ic_back,
                onClick = onBackClick,
                contentDescription = "بازگشت"
            )

            GlassPanel(
                modifier = Modifier.weight(1f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(22.dp)
            ) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GameAvatar(
                        name = stage?.characterName ?: "A",
                        imageUrl = stage?.characterAvatarUrl,
                        size = 34.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        GameText(
                            text = stage?.titleFa ?: "مکالمه انگلیسی",
                            size = 12.sp,
                            lineHeight = 17.sp,
                            bold = true,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        GameText(
                            text = stage?.characterName ?: "",
                            size = 11.sp,
                            color = Game.TextSecondary,
                            latin = true,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            GameButton(
                text = if (isFinishing) "در حال بررسی" else "پایان",
                onClick = onFinishConversationClick,
                modifier = Modifier.width(if (isFinishing) 124.dp else 82.dp),
                style = GameButtonStyle.Gold,
                enabled = !isFinishing,
                height = 40.dp,
                textSize = 13.sp,
                loading = isFinishing
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameChip(
                text = "هدف مرحله",
                icon = Res.drawable.ic_lamp,
                accent = Game.Mint,
                onClick = onObjectiveClick
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    GameText(
                        text = "نوبت ${turnsCount.coerceAtMost(maxTurns).fa()} از ${maxTurns.fa()}",
                        size = 11.sp,
                        color = Game.TextSecondary,
                        maxLines = 1
                    )
                }
                GameProgressBar(
                    progress = turnsCount.toFloat() / maxTurns,
                    modifier = Modifier.fillMaxWidth(),
                    height = 6.dp
                )
            }
            Spacer(modifier = Modifier.width(2.dp))
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatToolbarPreview() {
    AppTheme {
        ChatToolbar(
            stage = null,
            turnsCount = 3,
            isFinishing = false,
            onBackClick = {},
            onFinishConversationClick = {}
        )
    }
}
