package ir.aispeaking.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.chat.component.AppleChatTheme
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText

/**
 * 2026 Apple Liquid Glass Objective Sheet.
 *
 * Provides instant clarity on the learner's mission and background story:
 * - Translucent frosted glass bottom sheet.
 * - Goal container glowing in Serene Cyan & Royal Azure (strictly NO green).
 * - Scenario background in Warm Champagne Gold.
 * - Tactile Apple Primary Button for dismiss.
 */
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
            GameText(
                text = stage.titleFa,
                size = 18.sp,
                bold = true
            )

            // Mission Objective Container (Serene Cyan & Royal Azure glow)
            val goalShape = RoundedCornerShape(20.dp)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(goalShape)
                    .background(AppleChatTheme.PrimaryAzure.copy(alpha = 0.12f))
                    .border(1.dp, AppleChatTheme.PrimaryAzure.copy(alpha = 0.45f), goalShape)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GameText(
                    text = "هدف تو در این مرحله",
                    size = 13.sp,
                    bold = true,
                    color = AppleChatTheme.SuccessCyan
                )
                if (stage.targetObjectiveFa.isNotBlank()) {
                    GameText(
                        text = stage.targetObjectiveFa,
                        size = 14.sp,
                        lineHeight = 23.sp,
                        align = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )
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

            // Scenario Briefing Container
            if (stage.briefingFa.isNotBlank()) {
                val briefingShape = RoundedCornerShape(20.dp)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(briefingShape)
                        .background(AppleChatTheme.GlassPanelRaised)
                        .border(1.dp, AppleChatTheme.SpecularStrokeBrush, briefingShape)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(
                        text = "ماجرا چیست؟",
                        size = 13.sp,
                        bold = true,
                        color = AppleChatTheme.Gold
                    )
                    GameText(
                        text = stage.briefingFa,
                        size = 13.sp,
                        lineHeight = 22.sp,
                        color = Game.TextSecondary,
                        align = TextAlign.Justify,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Tactile Apple Primary Button
            GameButton(
                text = "متوجه شدم",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                height = 48.dp
            )
            Spacer(Modifier.padding(bottom = 6.dp))
        }
    }
}
