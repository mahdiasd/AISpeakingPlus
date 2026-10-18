package ir.aispeaking.chat.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
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
 * - Row 1: Back Navigation Button | Floating Glass Stage & Character Capsule | Action CTA (Finish)
 * - Row 2: Objective Glass Chip (Serene Cyan glow) | Luminous Progress Bar & Counter
 */
@Composable
fun ChatToolbar(
    modifier: Modifier = Modifier,
    stage: Stage?,
    userLevel: String? = null,
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
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1: Controls & Stage Identifier
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameIconButton(
                icon = Res.drawable.ic_back,
                onClick = onBackClick,
                contentDescription = "بازگشت",
                size = 42.dp,
                iconSize = 20.dp,
                container = Color(0x22FFFFFF),
                border = AppleChatTheme.SpecularBorderColor
            )

            // Liquid Glass Stage Info Capsule
            val headerShape = RoundedCornerShape(22.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(headerShape)
                    .background(AppleChatTheme.GlassPanel)
                    .border(1.dp, AppleChatTheme.SpecularStrokeBrush, headerShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    GameText(
                        text = stage?.titleFa ?: "مکالمه انگلیسی",
                        size = 13.sp,
                        lineHeight = 18.sp,
                        bold = true,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (!stage?.characterName.isNullOrBlank()) {
                            GameText(
                                text = stage.characterName,
                                size = 11.sp,
                                color = Game.TextSecondary,
                                latin = true,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (!userLevel.isNullOrBlank()) {
                            val levelShape = RoundedCornerShape(6.dp)
                            Box(
                                modifier = Modifier
                                    .clip(levelShape)
                                    .background(AppleChatTheme.PrimaryIndigo.copy(alpha = 0.18f))
                                    .border(0.75.dp, AppleChatTheme.PrimaryIndigo.copy(alpha = 0.45f), levelShape)
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                GameText(
                                    text = "سطح $userLevel",
                                    size = 10.sp,
                                    color = Color(0xFFA5A3F6),
                                    bold = true
                                )
                            }
                        }
                    }
                }
            }

            // Finish Conversation Button (Tactile Apple Pill)
            GameButton(
                text = if (isFinishing) "در حال بررسی" else "پایان",
                onClick = onFinishConversationClick,
                modifier = Modifier.width(if (isFinishing) 124.dp else 84.dp),
                style = GameButtonStyle.Gold,
                enabled = !isFinishing,
                height = 42.dp,
                textSize = 13.sp,
                loading = isFinishing
            )
        }

        // Row 2: Objective Trigger & Dynamic Progress Meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Objective Chip (Serene Cyan glow, strictly NO green)
            AppleObjectiveChip(
                text = "هدف مرحله",
                onClick = onObjectiveClick
            )

            // Progress Tracker HUD
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
                        .background(Color(0x28FFFFFF))
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

/**
 * Apple translucent Objective chip with Serene Cyan luminous glow.
 */
@Composable
private fun AppleObjectiveChip(
    text: String,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .applePressEffect(targetScale = 0.94f, onClick = onClick)
            .clip(shape)
            .background(Color(0xD9181A28))
            .border(
                1.dp,
                AppleChatTheme.SuccessCyan.copy(alpha = 0.45f),
                shape
            )
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_lamp),
            contentDescription = null,
            tint = AppleChatTheme.SuccessCyan,
            modifier = Modifier.size(15.dp)
        )
        GameText(
            text = text,
            size = 12.sp,
            bold = true,
            color = AppleChatTheme.SuccessCyan,
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
            turnsCount = 3,
            isFinishing = false,
            onBackClick = {},
            onFinishConversationClick = {}
        )
    }
}
