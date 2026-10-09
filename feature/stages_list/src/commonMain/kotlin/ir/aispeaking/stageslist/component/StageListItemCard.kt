package ir.aispeaking.stageslist.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_private
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameChip
import ir.aispeaking.sharedui.ui.game.GameStars
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import org.jetbrains.compose.resources.painterResource

/**
 * One stop on the journey path. A numbered node on a vertical track, and a card that says in plain
 * words what the stage is and what state it is in (current, done, locked and why).
 */
@Composable
fun StageListItemCard(
    stage: Stage,
    isActiveCurrentStage: Boolean,
    onStageClick: (Stage) -> Unit,
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val isLocked = stage.lockStatus != StageLockStatus.UNLOCKED
    val starsEarned = stage.userProgress?.stars ?: 0
    val hasPlayed = starsEarned > 0

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "stageItemScale"
    )

    val accent = when {
        isActiveCurrentStage -> Game.Mint
        stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Game.Gold
        stage.lockStatus == StageLockStatus.LOCKED_REGISTRATION -> Game.Violet
        isLocked -> Game.TextMuted
        hasPlayed -> Game.Gold
        else -> Game.Sky
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Track + node
        Box(
            modifier = Modifier
                .width(44.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = if (isFirst) 28.dp else 0.dp, bottom = if (isLast) 0.dp else 0.dp)
                    .width(3.dp)
                    .background(
                        if (isLast) Brush.verticalGradient(listOf(Game.Stroke, Color.Transparent))
                        else Brush.verticalGradient(listOf(Game.Stroke, Game.Stroke))
                    )
            )
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(if (isActiveCurrentStage) 44.dp else 38.dp)
                    .background(
                        if (isLocked) Brush.linearGradient(listOf(Color(0xFF2A3262), Color(0xFF1B2250)))
                        else Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.7f))),
                        CircleShape
                    )
                    .border(2.dp, if (isActiveCurrentStage) Color.White else accent.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                when (stage.lockStatus) {
                    StageLockStatus.LOCKED_SUBSCRIPTION -> Icon(
                        painter = painterResource(Res.drawable.ic_crown),
                        contentDescription = null,
                        tint = Game.Gold,
                        modifier = Modifier.size(18.dp)
                    )
                    StageLockStatus.LOCKED_PREVIOUS_STAGE, StageLockStatus.LOCKED_REGISTRATION -> Icon(
                        painter = painterResource(Res.drawable.ic_private),
                        contentDescription = null,
                        tint = Game.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    StageLockStatus.UNLOCKED -> GameText(
                        text = stage.orderIndex.fa(),
                        size = 15.sp,
                        bold = true,
                        color = if (isActiveCurrentStage || !hasPlayed) Color(0xFF04261C) else Color(0xFF3A2600)
                    )
                }
            }
        }

        // Card
        val shape = RoundedCornerShape(22.dp)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 12.dp)
                .scale(scale)
                .background(
                    if (isActiveCurrentStage) Game.PanelRaised else Game.Panel,
                    shape
                )
                .border(
                    if (isActiveCurrentStage) 2.dp else 1.dp,
                    if (isActiveCurrentStage) Game.Mint else Game.Stroke,
                    shape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onStageClick(stage) }
                )
                .padding(14.dp)
                .alpha(if (isLocked && !isActiveCurrentStage) 0.8f else 1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GameText(
                text = stage.titleFa,
                size = 14.sp,
                lineHeight = 21.sp,
                bold = true,
                color = if (isLocked) Game.TextSecondary else Game.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                GameText(
                    text = stage.characterName,
                    size = 12.sp,
                    latin = true,
                    color = Game.TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isLocked) {
                    GameStars(stars = starsEarned, size = 20.dp, spacing = 2.dp)
                } else {
                    GameText(text = "", size = 1.sp)
                }

                when {
                    isActiveCurrentStage -> GameChip(
                        text = "ادامه از اینجا",
                        accent = Game.Mint,
                        active = true
                    )
                    stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> GameChip(
                        text = "ویژه اشتراک",
                        icon = Res.drawable.ic_crown,
                        accent = Game.Gold,
                        active = true
                    )
                    stage.lockStatus == StageLockStatus.LOCKED_REGISTRATION -> GameChip(
                        text = "با ورود باز می‌شود",
                        accent = Game.Violet,
                        active = true
                    )
                    stage.lockStatus == StageLockStatus.LOCKED_PREVIOUS_STAGE -> GameChip(
                        text = "اول مرحله قبل",
                        icon = Res.drawable.ic_private,
                        accent = Game.TextSecondary
                    )
                    hasPlayed -> GameChip(text = "تکرار", accent = Game.Sky)
                    else -> GameChip(text = "شروع", accent = Game.Sky)
                }
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun StageListItemCardUnlockedPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        StageListItemCard(
            stage = Stage(
                id = "stage-1",
                orderIndex = 1,
                title = "Arrival at Heathrow",
                titleFa = "ورود به فرودگاه هیترو",
                briefing = "Heathrow airport",
                briefingFa = "فرودگاه هیترو",
                targetObjective = "Passport control",
                backgroundUrl = "",
                characterName = "Sarah Jenkins",
                lockStatus = StageLockStatus.UNLOCKED,
                userProgress = ir.aispeaking.domain.model.stage.StageProgress(stageId = "stage-1", stars = 3, completedAt = "")
            ),
            isActiveCurrentStage = true,
            onStageClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun StageListItemCardLockedPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        StageListItemCard(
            stage = Stage(
                id = "stage-2",
                orderIndex = 2,
                title = "Hotel Check-in",
                titleFa = "تحویل اتاق هتل",
                briefing = "Hotel check-in",
                briefingFa = "تحویل اتاق هتل",
                targetObjective = "Check in to room",
                backgroundUrl = "",
                characterName = "Emma Watson",
                lockStatus = StageLockStatus.LOCKED_SUBSCRIPTION
            ),
            isActiveCurrentStage = false,
            onStageClick = {}
        )
    }
}
