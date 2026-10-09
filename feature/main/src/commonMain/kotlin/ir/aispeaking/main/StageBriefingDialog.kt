package ir.aispeaking.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText

/** Mission briefing shown before a conversation starts: who, what happens, what to achieve. */
@Composable
fun StageBriefingDialog(
    visible: Boolean = true,
    stage: Stage,
    onDismiss: () -> Unit,
    onStartMission: (Stage) -> Unit
) {
    GameModal(visible = visible, onDismiss = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GameAvatar(
                name = stage.characterName,
                imageUrl = stage.characterAvatarUrl,
                size = 72.dp,
                ring = Game.Violet
            )
            GameText(
                text = stage.titleFa,
                size = 19.sp,
                lineHeight = 28.sp,
                bold = true,
                align = TextAlign.Center
            )
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = "با ${stage.characterName} صحبت می‌کنی",
                    size = 13.sp,
                    color = Game.TextSecondary,
                    align = TextAlign.Center
                )
            }

            InfoBlock(
                title = "هدف تو",
                accent = Game.Mint,
                body = stage.targetObjectiveFa.ifBlank { stage.targetObjective },
                latinHint = stage.targetObjective.takeIf { stage.targetObjectiveFa.isNotBlank() && it.isNotBlank() }
            )
            InfoBlock(
                title = "ماجرا چیست؟",
                accent = Game.Gold,
                body = stage.briefingFa
            )
        }

        // Actions stay pinned below the scrolling story so they are always reachable.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GameButton(
                text = "شروع گفتگو",
                onClick = { onStartMission(stage) },
                modifier = Modifier.fillMaxWidth(),
                icon = Res.drawable.ic_play
            )
            GameButton(
                text = "فعلاً نه",
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
private fun InfoBlock(
    title: String,
    accent: androidx.compose.ui.graphics.Color,
    body: String,
    latinHint: String? = null
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(accent.copy(alpha = 0.1f), shape)
            .border(1.dp, accent.copy(alpha = 0.35f), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GameText(text = title, size = 13.sp, bold = true, color = accent)
        GameText(text = body, size = 13.sp, lineHeight = 22.sp)
        if (latinHint != null) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = latinHint,
                    size = 12.sp,
                    lineHeight = 19.sp,
                    latin = true,
                    color = Game.TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun StageBriefingDialogPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        StageBriefingDialog(
            stage = Stage(
                id = "stage-1",
                orderIndex = 1,
                title = "Arrival at Heathrow",
                titleFa = "ورود به فرودگاه هیترو",
                briefing = "Passport check at Heathrow",
                briefingFa = "کنترل گذرنامه در هیترو",
                targetObjective = "Pass border control",
                targetObjectiveFa = "پاسخ به سوالات افسر مهاجرت و دریافت مهر ورود",
                backgroundUrl = "",
                characterName = "Sarah Jenkins"
            ),
            onDismiss = {},
            onStartMission = {}
        )
    }
}
