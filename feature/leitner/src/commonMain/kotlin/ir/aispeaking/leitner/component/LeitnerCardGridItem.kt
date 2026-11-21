package ir.aispeaking.leitner.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.model.leitner.LeitnerBox
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme

fun getBoxAccentColor(boxNumber: Int): Color {
    return when (boxNumber) {
        1 -> Color(0xFFFF7A45) // Coral / Warm Peach
        2 -> Color(0xFFF59E0B) // Amber / Warm Gold
        3 -> Color(0xFF22C55E) // Mint / Fresh Lime
        4 -> Color(0xFF0EA5E9) // Sky Blue
        5 -> Color(0xFF8B5CF6) // Royal Violet / Mastered
        else -> Color(0xFF06B6D4)
    }
}

@Composable
fun LeitnerCardGridItem(
    card: Flashcard,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "grid_card_press"
    )

    val boxColor = getBoxAccentColor(card.boxNumber)
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(if (isDark) Color(0xD9181A22) else Color(0xF2FFFFFF))
            .border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.08f),
                shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
    ) {
        // Dynamic Ambient Bottom Glow (like Pricing Plan Cards)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            boxColor.copy(alpha = if (isDark) 0.08f else 0.12f),
                            boxColor.copy(alpha = if (isDark) 0.28f else 0.35f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Box Tag & Review Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Box Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(boxColor.copy(alpha = if (isDark) 0.22f else 0.15f))
                        .border(1.dp, boxColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    GameText(
                        text = "جعبه ${card.boxNumber.fa()}",
                        size = 11.sp,
                        bold = true,
                        color = boxColor
                    )
                }

                // Due or Review count pill
                if (card.reviewCount > 0) {
                    GameText(
                        text = "دور ${card.reviewCount.fa()}",
                        size = 10.sp,
                        color = Game.TextSecondary
                    )
                }
            }

            // Center Content: Headword & Phonetics
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                GameText(
                    text = card.headword.ifBlank { card.customWord.orEmpty() },
                    size = 18.sp,
                    bold = true,
                    color = Game.TextPrimary,
                    align = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val phonetic = card.phonetic
                if (!phonetic.isNullOrBlank()) {
                    GameText(
                        text = phonetic,
                        size = 12.sp,
                        color = Game.TextSecondary,
                        align = TextAlign.Center,
                        maxLines = 1
                    )
                }

                val pos = card.partOfSpeech
                if (!pos.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color(0x22FFFFFF) else Color(0x14000000))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        GameText(
                            text = pos,
                            size = 10.sp,
                            color = Game.TextMuted,
                            align = TextAlign.Center
                        )
                    }
                }
            }

            // Bottom Prompt: Tap to flip hint
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameText(
                    text = "برای دیدن معنی لمس کنید 👆",
                    size = 10.sp,
                    color = boxColor.copy(alpha = 0.9f),
                    align = TextAlign.Center
                )
            }
        }
    }
}
