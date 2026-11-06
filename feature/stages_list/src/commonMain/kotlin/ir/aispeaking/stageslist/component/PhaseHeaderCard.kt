package ir.aispeaking.stageslist.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameProgressBar
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.game.fa

/**
 * Visual header separating story phases/units on the journey timeline.
 */
@Composable
fun PhaseHeaderCard(
    phaseNumber: Int,
    phaseTitleFa: String,
    completedStages: Int,
    totalStages: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalStages > 0) completedStages.toFloat() / totalStages else 0f
    val isPhaseCompleted = completedStages == totalStages && totalStages > 0

    GlassPanel(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPhaseCompleted) Game.Gold else Game.Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(
                            text = phaseNumber.fa(),
                            size = 15.sp,
                            bold = true,
                            color = if (isPhaseCompleted) Color(0xFF1F1600) else Color.White
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        GameText(
                            text = phaseTitleFa,
                            size = 15.sp,
                            bold = true,
                            color = Game.TextPrimary
                        )
                        GameText(
                            text = if (isPhaseCompleted) "تمام مراحل این فاز تکمیل شده ✨" else "${completedStages.fa()} از ${totalStages.fa()} مرحله تکمیل شده",
                            size = 12.sp,
                            color = if (isPhaseCompleted) Game.Gold else Game.TextSecondary
                        )
                    }
                }
            }

            GameProgressBar(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                height = 6.dp,
                color = if (isPhaseCompleted) Game.Gold else Game.Blue
            )
        }
    }
}
