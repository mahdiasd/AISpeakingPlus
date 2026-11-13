package ir.aispeaking.chat.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_lamp
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * 2026 Apple Liquid Glass HUD Toolbar for conversation.
 *
 * Structure:
 * - Single Compact Row: Objective Glass Chip (top-start) | Liquid Glass Stage & Character Capsule | Compact Finish CTA
 * - Conversation progress is hidden per product direction.
 */
@Composable
fun ChatToolbar(
    modifier: Modifier = Modifier,
    stage: Stage?,
    userLevel: String? = null,
    turnsCount: Int = 0,
    isFinishing: Boolean = false,
    showProgress: Boolean = false,
    onBackClick: () -> Unit = {},
    onObjectiveClick: () -> Unit = {},
    onHeaderClick: () -> Unit = {},
    onFinishConversationClick: () -> Unit,
    objectiveModifier: Modifier = Modifier
) {
    val maxTurns = stage?.maxTurns?.coerceAtLeast(1) ?: 10
    val isDark = AppTheme.isDark

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Single Compact Row: Objective Action, Stage Details, and Compact Finish CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Objective Glass Chip (Serene Cyan glow) - at Start anchor (replaces back button)
            AppleObjectiveChip(
                text = "هدف مرحله",
                onClick = onObjectiveClick,
                modifier = objectiveModifier
            )

            // Liquid Glass Stage Info Capsule
            val headerShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .clip(headerShape)
                    .background(AppleChatTheme.GlassPanel)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, headerShape)
                    .clickable(onClick = onHeaderClick)
                    .padding(horizontal = 10.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GameText(
                        text = stage?.titleFa ?: "مکالمه انگلیسی",
                        size = 13.sp,
                        lineHeight = 17.sp,
                        bold = true,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    if (!userLevel.isNullOrBlank()) {
                        GameText(
                            text = "سطح $userLevel",
                            size = 11.sp,
                            lineHeight = 15.sp,
                            color = Game.TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Compact Finish Conversation Button (Tactile Apple Pill)
            GameButton(
                text = if (isFinishing) "در حال بررسی" else "پایان",
                onClick = onFinishConversationClick,
                modifier = Modifier.width(if (isFinishing) 104.dp else 68.dp),
                style = GameButtonStyle.Gold,
                enabled = !isFinishing && stage != null,
                height = 36.dp,
                textSize = 12.sp,
                loading = isFinishing
            )
        }

        // Hidden by default: Progress Tracker HUD (retained for future toggle)
        if (showProgress) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameText(
                            text = "پیشرفت گفتگو: ${turnsCount.coerceAtMost(maxTurns).fa()} از ${maxTurns.fa()} پیام",
                            size = 11.sp,
                            color = Game.TextSecondary,
                            maxLines = 1
                        )
                    }

                    // Apple Luminous Liquid Progress Bar
                    val progress = (turnsCount.toFloat() / maxTurns).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = progress,
                        animationSpec = tween(350),
                        label = "chat_turns_progress"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x28FFFFFF) else Color(0x1F000000))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(animatedProgress)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AppleChatTheme.SuccessCyan,
                                        AppleChatTheme.PrimaryAzure
                                    )
                                )
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.width(2.dp))
            }
        }
    }
}

/**
 * Apple translucent Objective chip with Serene Cyan luminous glow.
 */
@Composable
private fun AppleObjectiveChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(14.dp)
    val accent = if (isDark) AppleChatTheme.SuccessCyan else AppleChatTheme.PrimaryAzure
    Row(
        modifier = modifier
            .height(36.dp)
            .applePressEffect(targetScale = 0.94f, onClick = onClick)
            .clip(shape)
            .background(if (isDark) Color(0xD9181A28) else Color(0xD9EBF5FF))
            .border(
                1.dp,
                accent.copy(alpha = if (isDark) 0.45f else 0.5f),
                shape
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_lamp),
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(15.dp)
        )
        GameText(
            text = text,
            size = 12.sp,
            bold = true,
            color = accent,
            maxLines = 1
        )
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ChatToolbarPreview() {
    AppTheme {
        ChatToolbar(
            stage = null,
            userLevel = "B1",
            turnsCount = 3,
            isFinishing = false,
            onBackClick = {},
            onFinishConversationClick = {}
        )
    }
}
