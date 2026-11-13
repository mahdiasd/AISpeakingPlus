package ir.aispeaking.stageslist.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

/**
 * Apple-grade frosted glass section header separating phases on the journey map.
 * Visually distinct from individual stage cards with:
 * - Rich accent tinted glass surface (sapphire for active, amber/gold for completed)
 * - Apple specular edge highlight at the top rim
 * - Category micro-pill and bold phase title
 * - Smooth Apple animated progress bar
 */
@Composable
fun PhaseHeaderCard(
    phaseNumber: Int,
    phaseTitleFa: String,
    completedStages: Int,
    totalStages: Int,
    modifier: Modifier = Modifier
) {
    val isPhaseCompleted = completedStages == totalStages && totalStages > 0
    val progress = if (totalStages > 0) completedStages.toFloat() / totalStages else 0f
    val isDark = AppTheme.isDark
    val shape = RoundedCornerShape(22.dp)

    // Distinct tinted glass surfaces
    val surfaceBrush = if (isDark) {
        if (isPhaseCompleted) {
            Brush.linearGradient(
                listOf(Color(0xE6251E12), Color(0xCC1A140C))
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xEB151D33), Color(0xD90E1424))
            )
        }
    } else {
        if (isPhaseCompleted) {
            Brush.linearGradient(
                listOf(Color(0xFFFFFDF5), Color(0xFFFFF8E1))
            )
        } else {
            Brush.linearGradient(
                listOf(Color(0xFFF3F7FF), Color(0xFFE9F2FE))
            )
        }
    }

    val specularStroke = Brush.verticalGradient(
        if (isDark) {
            if (isPhaseCompleted) {
                listOf(Game.Gold.copy(alpha = 0.55f), Game.Gold.copy(alpha = 0.18f))
            } else {
                listOf(Game.Blue.copy(alpha = 0.55f), Game.Blue.copy(alpha = 0.18f))
            }
        } else {
            if (isPhaseCompleted) {
                listOf(Game.Gold.copy(alpha = 0.5f), Game.Gold.copy(alpha = 0.2f))
            } else {
                listOf(Game.Blue.copy(alpha = 0.45f), Game.Blue.copy(alpha = 0.18f))
            }
        }
    )

    val topRimBrush = Brush.horizontalGradient(
        if (isPhaseCompleted) {
            listOf(Color.Transparent, Game.Gold.copy(alpha = 0.85f), Game.GoldDeep.copy(alpha = 0.85f), Color.Transparent)
        } else {
            listOf(Color.Transparent, Game.Blue.copy(alpha = 0.85f), Game.Sky.copy(alpha = 0.85f), Color.Transparent)
        }
    )

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 300f),
        label = "phaseProgress"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(surfaceBrush)
            .border(1.2.dp, specularStroke, shape)
    ) {
        // Specular top edge highlight line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(topRimBrush)
        )

        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 3D Phase Orb Badge
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            if (isPhaseCompleted) {
                                Brush.linearGradient(listOf(Color(0xFFFFD60A), Color(0xFFD4A000)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFF0A84FF), Color(0xFF0056B3)))
                            }
                        )
                        .border(1.5.dp, Color.White.copy(alpha = 0.55f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPhaseCompleted) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_done),
                            contentDescription = null,
                            tint = Color(0xFF1F1600),
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        GameText(
                            text = phaseNumber.fa(),
                            size = 16.sp,
                            bold = true,
                            color = Color.White
                        )
                    }
                }

                // Phase Title & Category micro-pill
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(
                                    if (isPhaseCompleted) Game.Gold.copy(alpha = 0.16f)
                                    else Game.Blue.copy(alpha = 0.16f)
                                )
                                .border(
                                    1.dp,
                                    if (isPhaseCompleted) Game.Gold.copy(alpha = 0.35f)
                                    else Game.Blue.copy(alpha = 0.35f),
                                    RoundedCornerShape(50)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            GameText(
                                text = "فاز ${phaseNumber.fa()}",
                                size = 10.sp,
                                bold = true,
                                color = if (isPhaseCompleted) Game.Gold else Game.Sky
                            )
                        }
                    }

                    GameText(
                        text = phaseTitleFa,
                        size = 15.sp,
                        bold = true,
                        color = Game.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Status chip (completed or fraction)
                if (isPhaseCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Game.Gold.copy(alpha = 0.18f))
                            .border(1.dp, Game.Gold.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        GameText(
                            text = "تکمیل شد ✨",
                            size = 11.sp,
                            bold = true,
                            color = Game.Gold
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x28FFFFFF) else Color(0x1F000000))
                            .border(
                                1.dp,
                                if (isDark) Color(0x28FFFFFF) else Color(0x1A000000),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 4.dp)
                    ) {
                        GameText(
                            text = "${completedStages.fa()} از ${totalStages.fa()}",
                            size = 11.sp,
                            bold = true,
                            color = Game.TextSecondary
                        )
                    }
                }
            }

            // Slim Apple progress indicator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (isDark) Color(0x2EFFFFFF) else Color(0x1F000000))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isPhaseCompleted) {
                                Brush.horizontalGradient(listOf(Game.Gold, Game.GoldDeep))
                            } else {
                                Brush.horizontalGradient(listOf(Game.Blue, Game.Sky))
                            }
                        )
                )
            }
        }
    }
}
