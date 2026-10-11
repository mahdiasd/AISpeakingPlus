package ir.aispeaking.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_close
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_scenario
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameAvatar
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameIconButton
import ir.aispeaking.sharedui.ui.game.GameModal
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import org.jetbrains.compose.resources.painterResource

/**
 * Apple-style Mission Briefing Sheet presented before entering the conversation.
 * Presents character context, learning objectives, and scenario clearly with Apple HIG hierarchy.
 */
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
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation Row: Stage badge & Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Game.Blue.copy(alpha = 0.16f))
                        .border(1.dp, Game.Blue.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    GameText(
                        text = "مرحله ${stage.orderIndex.fa()}",
                        size = 12.sp,
                        bold = true,
                        color = Game.Blue
                    )
                }

                GameIconButton(
                    icon = Res.drawable.ic_close,
                    onClick = onDismiss,
                    contentDescription = "بستن",
                    size = 32.dp,
                    iconSize = 14.dp,
                    tint = Game.TextSecondary,
                    container = Color(0x1FFFFFFF),
                    border = Color(0x14FFFFFF)
                )
            }

            // Hero Section: Character Presence & Stage Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GameAvatar(
                    name = stage.characterName,
                    imageUrl = stage.characterAvatarUrl,
                    size = 54.dp,
                    ring = Game.StrokeStrong
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    GameText(
                        text = stage.titleFa,
                        size = 18.sp,
                        lineHeight = 26.sp,
                        bold = true,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(Game.Mint, CircleShape)
                            )
                            GameText(
                                text = "${stage.characterName} · AI Partner",
                                size = 12.sp,
                                color = Game.TextSecondary,
                                latin = true,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Apple Grouped Inset Card 1: Main Objective
            val targetFa = stage.targetObjectiveFa.ifBlank { stage.targetObjective }
            val targetEn = stage.targetObjective.takeIf { stage.targetObjectiveFa.isNotBlank() && it.isNotBlank() }
            AppleInsetCard(
                icon = Res.drawable.ic_done,
                iconTint = Game.Mint,
                badgeBg = Game.Mint.copy(alpha = 0.16f),
                title = "هدف این مکالمه",
                body = targetFa,
                latinSubtext = targetEn
            )

            // Apple Grouped Inset Card 2: Scenario & Story
            if (stage.briefingFa.isNotBlank()) {
                AppleInsetCard(
                    icon = Res.drawable.ic_scenario,
                    iconTint = Game.Sky,
                    badgeBg = Game.Sky.copy(alpha = 0.16f),
                    title = "سناریو و موقعیت",
                    body = stage.briefingFa
                )
            }

            // Quick Info Tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickTag(text = "مکالمه صوتی و متنی", modifier = Modifier.weight(1f))
                QuickTag(text = "امتیاز تا ۳ ستاره", modifier = Modifier.weight(1f))
            }
        }

        // Action Buttons pinned at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameButton(
                text = "شروع گفتگو",
                onClick = { onStartMission(stage) },
                modifier = Modifier.fillMaxWidth(),
                icon = Res.drawable.ic_play,
                height = 50.dp,
                textSize = 15.sp,
                style = GameButtonStyle.Primary
            )
            GameButton(
                text = "فعلاً نه",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                style = GameButtonStyle.Glass,
                height = 42.dp,
                textSize = 14.sp
            )
        }
    }
}

@Composable
private fun AppleInsetCard(
    icon: org.jetbrains.compose.resources.DrawableResource,
    iconTint: Color,
    badgeBg: Color,
    title: String,
    body: String,
    latinSubtext: String? = null
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Game.PanelRaised)
            .border(1.dp, Color(0x18FFFFFF), shape)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
            }
            GameText(
                text = title,
                size = 13.sp,
                bold = true,
                color = iconTint
            )
        }

        GameText(
            text = body,
            size = 13.sp,
            lineHeight = 22.sp,
            color = Game.TextPrimary
        )

        if (latinSubtext != null) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x14FFFFFF))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    GameText(
                        text = latinSubtext,
                        size = 12.sp,
                        lineHeight = 18.sp,
                        latin = true,
                        color = Game.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickTag(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x12FFFFFF))
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        GameText(
            text = text,
            size = 11.sp,
            color = Game.TextSecondary,
            align = TextAlign.Center
        )
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
