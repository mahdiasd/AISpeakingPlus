package ir.aispeaking.sharedui.ui.guide

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Frosted Glass Champagne Gold Guide Card, styled identically to the Chat GrammarTipCard.
 */
@Composable
fun AppGuideCard(
    step: GuideStep,
    stepIndex: Int,
    totalSteps: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(18.dp)
    val accent = if (isDark) Game.Gold else Game.GoldDeep

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier
                .widthIn(min = 260.dp, max = 310.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = shape,
                    ambientColor = Color.Black.copy(alpha = 0.35f),
                    spotColor = accent.copy(alpha = 0.25f),
                    clip = false
                )
                .clip(shape)
                .background(if (isDark) Color(0xF21C1A17) else Color(0xFFFFFDF5))
                .border(1.25.dp, accent.copy(alpha = if (isDark) 0.55f else 0.45f), shape)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Icon + Title + Step Counter / Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(step.icon ?: Res.drawable.ic_lamp),
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                }

                GameText(
                    text = step.title,
                    size = 13.5.sp,
                    bold = true,
                    color = accent,
                    modifier = Modifier.weight(1f)
                )

                if (totalSteps > 1) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        GameText(
                            text = "${(stepIndex + 1).fa()} از ${totalSteps.fa()}",
                            size = 10.sp,
                            bold = true,
                            color = Game.TextSecondary
                        )
                    }
                }

                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "بستن",
                    tint = Game.TextMuted,
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSkip
                        )
                )
            }

            // Body: Step Description
            AnimatedContent(
                targetState = step.description,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "guide_step_desc"
            ) { desc ->
                GameText(
                    text = desc,
                    size = 12.5.sp,
                    lineHeight = 19.sp,
                    color = Game.TextPrimary,
                    align = TextAlign.Justify
                )
            }

            // Footer Actions: Skip + Next
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Skip Button
                GameText(
                    text = "رد شدن",
                    size = 12.sp,
                    color = Game.TextSecondary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSkip
                        )
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (stepIndex > 0) {
                        GameText(
                            text = "قبلی",
                            size = 12.sp,
                            color = Game.TextSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = onPrevious
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }

                    // Next / Done button
                    val isLast = stepIndex >= totalSteps - 1
                    val buttonShape = RoundedCornerShape(10.dp)
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 2.dp, shape = buttonShape)
                            .clip(buttonShape)
                            .background(accent)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onNext
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(
                            text = if (isLast) "متوجه شدم" else "بعدی",
                            size = 12.sp,
                            bold = true,
                            color = if (isDark) Color.Black else Color.White
                        )
                    }
                }
            }
        }
    }
}
