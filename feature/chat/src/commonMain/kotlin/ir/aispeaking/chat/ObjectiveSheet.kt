package ir.aispeaking.chat

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText

/** Reminder of the scenario and the mission goal, available any time during the conversation. */
@Composable
fun ObjectiveSheet(
    visible: Boolean,
    stage: Stage?,
    onDismiss: () -> Unit
) {
    GameSheet(visible = visible, onDismiss = onDismiss) {
        if (stage == null) return@GameSheet
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GameText(text = stage.titleFa, size = 18.sp, bold = true)

            val goalShape = RoundedCornerShape(18.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Game.Mint.copy(alpha = 0.12f), goalShape)
                    .border(1.dp, Game.Mint.copy(alpha = 0.4f), goalShape)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameText(text = "هدف تو در این مرحله", size = 13.sp, bold = true, color = Game.Mint)
                if (stage.targetObjectiveFa.isNotBlank()) {
                    GameText(text = stage.targetObjectiveFa, size = 14.sp, lineHeight = 23.sp)
                }
                if (stage.targetObjective.isNotBlank()) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        GameText(
                            text = stage.targetObjective,
                            size = 13.sp,
                            lineHeight = 20.sp,
                            latin = true,
                            color = Game.TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            if (stage.briefingFa.isNotBlank()) {
                val shape = RoundedCornerShape(18.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Game.PanelRaised, shape)
                        .border(1.dp, Game.Stroke, shape)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(text = "ماجرا چیست؟", size = 13.sp, bold = true, color = Game.Gold)
                    GameText(
                        text = stage.briefingFa,
                        size = 13.sp,
                        lineHeight = 22.sp,
                        color = Game.TextSecondary
                    )
                }
            }

            GameButton(
                text = "متوجه شدم",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.foundation.layout.Spacer(Modifier.padding(bottom = 4.dp))
        }
    }
}
