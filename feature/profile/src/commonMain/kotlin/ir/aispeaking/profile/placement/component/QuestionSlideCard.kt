package ir.aispeaking.profile.placement.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.placement.PlacementQuestion
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun QuestionSlideCard(
    questionIndex: Int,
    question: PlacementQuestion,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val cardBg = if (isDark) Color(0x1AFFFFFF) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000)

    val (levelColor, levelBg) = when (question.level) {
        LanguageLevel.A1 -> Pair(Color(0xFF10B981), if (isDark) Color(0x2210B981) else Color(0x1F10B981))
        LanguageLevel.A2 -> Pair(Color(0xFF06B6D4), if (isDark) Color(0x2206B6D4) else Color(0x1F06B6D4))
        LanguageLevel.B1 -> Pair(Color(0xFF3B82F6), if (isDark) Color(0x223B82F6) else Color(0x1F3B82F6))
        LanguageLevel.B2 -> Pair(Color(0xFF6366F1), if (isDark) Color(0x226366F1) else Color(0x1F6366F1))
        LanguageLevel.C1, LanguageLevel.C2 -> Pair(Color(0xFFA855F7), if (isDark) Color(0x22A855F7) else Color(0x1FA855F7))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question Header Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Tag Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(levelBg)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GameText(
                            text = "سطح ${question.level.name}",
                            size = 12.sp,
                            bold = true,
                            color = levelColor
                        )
                    }

                    GameText(
                        text = "سوال ${questionIndex + 1}",
                        size = 12.sp,
                        color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                    )
                }

                // Question Prompt (English, LTR)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Text(
                        text = question.questionText,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.SansSerif,
                        color = if (isDark) Game.TextPrimary else Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        GameText(
            text = "گزینه مناسب را انتخاب کنید:",
            size = 13.sp,
            color = if (isDark) Game.TextSecondary else Color(0xFF64748B),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Options List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            question.options.forEachIndexed { index, option ->
                val isSelected = selectedOption == option
                OptionSelectableCard(
                    optionIndex = index,
                    optionText = option,
                    isSelected = isSelected,
                    onSelect = { onOptionSelected(option) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
