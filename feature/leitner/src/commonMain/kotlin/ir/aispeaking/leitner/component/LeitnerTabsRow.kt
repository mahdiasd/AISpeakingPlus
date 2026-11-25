package ir.aispeaking.leitner.component

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.leitner.LeitnerBox
import ir.aispeaking.leitner.model.LeitnerTab
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun LeitnerTabsRow(
    selectedTab: LeitnerTab,
    dueCount: Int,
    allCount: Int,
    boxCounts: Map<Int, Long>,
    onSelectTab: (LeitnerTab) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(LeitnerTab.allTabs) { tab ->
            val isSelected = when (tab) {
                is LeitnerTab.DueToday -> selectedTab is LeitnerTab.DueToday
                is LeitnerTab.All -> selectedTab is LeitnerTab.All
                is LeitnerTab.BoxTab -> selectedTab is LeitnerTab.BoxTab && (selectedTab as LeitnerTab.BoxTab).box == tab.box
            }

            val count = when (tab) {
                is LeitnerTab.DueToday -> dueCount
                is LeitnerTab.All -> allCount
                is LeitnerTab.BoxTab -> boxCounts[tab.box.boxNumber]?.toInt() ?: 0
            }

            LeitnerTabChip(
                tab = tab,
                isSelected = isSelected,
                count = count,
                onClick = { onSelectTab(tab) }
            )
        }
    }
}

@Composable
private fun LeitnerTabChip(
    tab: LeitnerTab,
    isSelected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val isDark = AppTheme.isDark
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "tab_chip_scale"
    )

    val accentColor = when (tab) {
        is LeitnerTab.DueToday -> Color(0xFF06B6D4) // Serene Cyan
        is LeitnerTab.All -> Color(0xFF6366F1) // Indigo
        is LeitnerTab.BoxTab -> when (tab.box) {
            LeitnerBox.BOX_1 -> Color(0xFFFF7A45) // Coral
            LeitnerBox.BOX_2 -> Color(0xFFF59E0B) // Amber
            LeitnerBox.BOX_3 -> Color(0xFF22C55E) // Mint Green
            LeitnerBox.BOX_4 -> Color(0xFF0EA5E9) // Sky Blue
            LeitnerBox.BOX_5 -> Color(0xFF8B5CF6) // Purple
        }
    }

    val containerColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = if (isDark) 0.24f else 0.16f)
            isDark -> Color(0x1AFFFFFF)
            else -> Color(0xCCFFFFFF)
        },
        label = "tab_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> accentColor.copy(alpha = if (isDark) 0.75f else 0.85f)
            isDark -> Color(0x28FFFFFF)
            else -> Color(0x1F000000)
        },
        label = "tab_border"
    )

    val labelText = when (tab) {
        is LeitnerTab.DueToday -> "🎯 امروز برای مرور"
        is LeitnerTab.All -> "همه"
        is LeitnerTab.BoxTab -> tab.box.titleFa.split(" ").take(2).joinToString(" ")
    }

    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(containerColor)
            .border(if (isSelected) 1.5.dp else 1.dp, borderColor, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        GameText(
            text = labelText,
            size = 13.sp,
            bold = isSelected,
            color = if (isSelected) accentColor else Game.TextPrimary
        )

        // Count Pill
        if (count > 0 || tab is LeitnerTab.DueToday) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (isSelected) accentColor
                        else if (isDark) Color(0x33FFFFFF)
                        else Color(0x1A000000)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = count.fa(),
                    size = 11.sp,
                    bold = true,
                    color = if (isSelected) Color.White else Game.TextSecondary
                )
            }
        }
    }
}
