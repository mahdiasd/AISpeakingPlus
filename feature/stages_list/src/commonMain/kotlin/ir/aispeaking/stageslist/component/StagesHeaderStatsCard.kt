package ir.aispeaking.stageslist.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameProgressBar
import ir.aispeaking.sharedui.ui.game.GameStatPill
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.GlassPanel
import ir.aispeaking.sharedui.ui.game.fa

/** Pinned summary of the whole journey: stars collected and stages completed. */
@Composable
fun StagesHeaderStatsCard(
    totalStars: Int,
    maxStars: Int,
    completedStages: Int,
    totalStages: Int,
    progressPercentage: Float,
    modifier: Modifier = Modifier
) {
    GlassPanel(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    GameText(text = "پیشرفت ماجراجویی", size = 12.sp, color = Game.TextSecondary)
                    GameText(
                        text = "${completedStages.fa()} از ${totalStages.fa()} مرحله",
                        size = 17.sp,
                        bold = true
                    )
                }
                GameStatPill(
                    text = "${totalStars.fa()} / ${maxStars.fa()}",
                    icon = Res.drawable.ic_star,
                    accent = Game.Gold
                )
            }
            GameProgressBar(
                progress = progressPercentage,
                modifier = Modifier.fillMaxWidth(),
                height = 8.dp,
                color = Game.Gold
            )
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun StagesHeaderStatsCardPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        StagesHeaderStatsCard(
            totalStars = 7,
            maxStars = 45,
            completedStages = 3,
            totalStages = 15,
            progressPercentage = 7f / 45f
        )
    }
}
