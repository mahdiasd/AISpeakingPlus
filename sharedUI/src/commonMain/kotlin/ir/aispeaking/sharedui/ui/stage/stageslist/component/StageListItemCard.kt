package ir.aispeaking.sharedui.ui.stage.stageslist.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_crown
import ir.aispeaking.sharedui.ic_play
import ir.aispeaking.sharedui.ic_private
import ir.aispeaking.sharedui.ic_star
import ir.aispeaking.sharedui.ic_star_outline
import org.jetbrains.compose.resources.painterResource

/**
 * Compact stage list item card designed with Emil Kowalski's principles:
 * - Scannable horizontal layout (~80dp height)
 * - Press scale feedback (scale 0.98 on active press)
 * - Clear visual hierarchy and rich contrast between active, completed, and locked stages
 */
@Composable
fun StageListItemCard(
    stage: Stage,
    isActiveCurrentStage: Boolean,
    onStageClick: (Stage) -> Unit,
    modifier: Modifier = Modifier
) {
    val isLocked = stage.lockStatus != StageLockStatus.UNLOCKED
    val starsEarned = stage.userProgress?.stars ?: 0
    val hasPlayed = starsEarned > 0

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "stageItemScale"
    )

    val containerColor = when {
        isActiveCurrentStage -> Color(0xFF16213E)
        isLocked -> Color(0x990D1424)
        else -> Color(0xFF0F172A)
    }

    val borderStroke = when {
        isActiveCurrentStage -> BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(
                listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFF818CF8))
            )
        )
        isLocked -> BorderStroke(1.dp, Color(0x14FFFFFF))
        else -> BorderStroke(1.dp, Color(0x26FFFFFF))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onStageClick(stage) }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActiveCurrentStage) 6.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Stage Order / Number Badge (Right side in RTL)
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isActiveCurrentStage -> Brush.linearGradient(
                                listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                            )
                            isLocked -> Brush.linearGradient(
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
                            )
                            hasPlayed -> Brush.linearGradient(
                                listOf(Color(0xFF6366F1), Color(0xFF4F46E5))
                            )
                            else -> Brush.linearGradient(
                                listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                            )
                        }
                    )
                    .then(
                        if (isActiveCurrentStage) {
                            Modifier.border(1.5.dp, Color(0xFFFFD700), CircleShape)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked && stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_crown),
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isLocked && stage.lockStatus == StageLockStatus.LOCKED_PREVIOUS_STAGE) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_private),
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                } else {
                    Text(
                        text = "${stage.orderIndex}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Middle Content: Titles, Sub-titles, and Stars/Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title Fa + Active Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = stage.titleFa,
                        color = if (isLocked) Color(0xFF94A3B8) else Color.White,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isActiveCurrentStage) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0x33F59E0B))
                                .padding(horizontal = 5.dp, vertical = 1.5.dp)
                        ) {
                            Text(
                                text = "فعلی",
                                color = Color(0xFFFFD700),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Title En
                Text(
                    text = stage.title,
                    color = if (isLocked) Color(0xFF64748B) else Color(0xFFA5B4FC),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Metadata Row: Stars + Character
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isLocked) {
                        // Stars Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(3) { starIndex ->
                                val isEarned = starIndex < starsEarned
                                Icon(
                                    painter = painterResource(
                                        if (isEarned) Res.drawable.ic_star else Res.drawable.ic_star_outline
                                    ),
                                    contentDescription = null,
                                    tint = if (isEarned) Color(0xFFFFD700) else Color(0x33FFFFFF),
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Text(
                            text = "•",
                            color = Color(0xFF475569),
                            fontSize = 10.sp
                        )
                    }

                    Text(
                        text = "هم‌صحبت: ${stage.characterName}",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Action CTA / Badge (Left side in RTL)
            val buttonText = when {
                isLocked && stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> "ویژه"
                isLocked -> "قفل"
                isActiveCurrentStage -> "شروع"
                hasPlayed -> "تکرار"
                else -> "ورود"
            }

            val buttonColor = when {
                isActiveCurrentStage -> Color(0xFF10B981) // Emerald
                stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Color(0xFFD97706)
                isLocked -> Color(0xFF1E293B)
                hasPlayed -> Color(0xFF4F46E5)
                else -> Color(0xFF2563EB)
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(buttonColor)
                    .clickable { onStageClick(stage) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    if (!isLocked) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_play),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    } else if (stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_crown),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Text(
                        text = buttonText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isLocked && stage.lockStatus != StageLockStatus.LOCKED_SUBSCRIPTION) Color(0xFF94A3B8) else Color.White
                    )
                }
            }
        }
    }
}
