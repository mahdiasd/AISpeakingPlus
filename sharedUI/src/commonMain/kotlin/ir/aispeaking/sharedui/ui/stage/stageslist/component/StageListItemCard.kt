package ir.aispeaking.sharedui.ui.stage.stageslist.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

    val containerColor = when {
        isActiveCurrentStage -> Color(0xFF1E293B)
        isLocked -> Color(0xFF0D1424)
        else -> Color(0xFF131D31)
    }

    val borderStroke = when {
        isActiveCurrentStage -> BorderStroke(
            2.dp,
            Brush.horizontalGradient(
                listOf(Color(0xFFFFD700), Color(0xFFF59E0B), Color(0xFF818CF8))
            )
        )
        isLocked -> BorderStroke(1.dp, Color(0x1AFFFFFF))
        else -> BorderStroke(1.dp, Color(0x33475569))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onStageClick(stage) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActiveCurrentStage) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Order Badge + Active Badge + Lock/Star indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stage Order Circle Badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActiveCurrentStage) {
                                    Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))
                                } else if (isLocked) {
                                    Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF4F46E5)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${stage.orderIndex}",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Active Stage Pill Badge
                    if (isActiveCurrentStage) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0x33F59E0B))
                                .border(1.dp, Color(0x88F59E0B), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "مرحله کنونی شما",
                                color = Color(0xFFFFD700),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Lock icon or Stars Row
                if (isLocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val lockIcon = when (stage.lockStatus) {
                            StageLockStatus.LOCKED_SUBSCRIPTION -> Res.drawable.ic_crown
                            else -> Res.drawable.ic_private
                        }
                        val lockLabel = when (stage.lockStatus) {
                            StageLockStatus.LOCKED_SUBSCRIPTION -> "ویژه"
                            StageLockStatus.LOCKED_REGISTRATION -> "ثبت‌نام"
                            else -> "قفل"
                        }
                        Icon(
                            painter = painterResource(lockIcon),
                            contentDescription = null,
                            tint = if (stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION) Color(0xFFFFD700) else Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = lockLabel,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // Stars Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { starIndex ->
                            val isEarned = starIndex < starsEarned
                            Icon(
                                painter = painterResource(
                                    if (isEarned) Res.drawable.ic_star else Res.drawable.ic_star_outline
                                ),
                                contentDescription = null,
                                tint = if (isEarned) Color(0xFFFFD700) else Color(0x44FFFFFF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage Titles
            Text(
                text = stage.titleFa,
                color = if (isLocked) Color(0xFF94A3B8) else Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = stage.title,
                color = if (isLocked) Color(0xFF64748B) else Color(0xFFA5B4FC),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (stage.targetObjectiveFa.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stage.targetObjectiveFa,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left character/partner name tag
                Text(
                    text = "هم‌صحبت: ${stage.characterName}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )

                // Action Button
                val buttonText = when {
                    isLocked && stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> "خرید اشتراک"
                    isLocked -> "قفل"
                    isActiveCurrentStage -> "شروع مرحله"
                    hasPlayed -> "تکرار مرحله"
                    else -> "ورود"
                }

                Button(
                    onClick = { onStageClick(stage) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            isActiveCurrentStage -> Color(0xFF10B981)
                            stage.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION -> Color(0xFFD97706)
                            isLocked -> Color(0xFF334155)
                            hasPlayed -> Color(0xFF4F46E5)
                            else -> Color(0xFF2563EB)
                        }
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (!isLocked) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_play),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = buttonText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
