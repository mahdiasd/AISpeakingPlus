package ir.aispeaking.profile.placement.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ir.aispeaking.domain.model.placement.QuestionReviewItem
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun QuestionReviewDialog(
    visible: Boolean,
    reviewItems: List<QuestionReviewItem>,
    onDismiss: () -> Unit
) {
    if (!visible) return

    val isDark = AppTheme.isDark
    val dialogBg = if (isDark) Color(0xFF13151D) else Color(0xFFF8FAFC)
    val cardBg = if (isDark) Color(0x1AFFFFFF) else Color(0xFFFFFFFF)
    val cardBorder = if (isDark) Color(0x1FFFFFFF) else Color(0x14000000)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(dialogBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GameText(
                            text = "مرور پاسخ‌های آزمون",
                            size = 18.sp,
                            bold = true,
                            color = if (isDark) Game.TextPrimary else Color(0xFF0F172A)
                        )

                        GameButton(
                            text = "بستن",
                            style = GameButtonStyle.Glass,
                            onClick = onDismiss
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // List of all question reviews
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        itemsIndexed(reviewItems) { index, item ->
                            val statusBg = if (item.isCorrect) {
                                if (isDark) Color(0x2210B981) else Color(0x1F10B981)
                            } else {
                                if (isDark) Color(0x22EF4444) else Color(0x1FEF4444)
                            }

                            val statusText = if (item.isCorrect) "پاسخ صحیح" else "پاسخ اشتباه"
                            val statusColor = if (item.isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(20.dp))
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Row: Question # and Status Badge
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        GameText(
                                            text = "سوال ${index + 1} (${item.level.name})",
                                            size = 13.sp,
                                            bold = true,
                                            color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(statusBg)
                                                .padding(horizontal = 10.dp, vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GameText(
                                                text = statusText,
                                                size = 11.sp,
                                                bold = true,
                                                color = statusColor
                                            )
                                        }
                                    }

                                    // Question prompt in English
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(
                                            text = item.questionText,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            fontWeight = FontWeight.Medium,
                                            fontFamily = FontFamily.SansSerif,
                                            color = if (isDark) Game.TextPrimary else Color(0xFF0F172A),
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }

                                    // Answer Comparison
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // User's Answer Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            GameText(
                                                text = "پاسخ شما:",
                                                size = 12.sp,
                                                color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                                            )

                                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                Text(
                                                    text = item.selectedOption ?: "(پاسخ نداده‌اید)",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontFamily = FontFamily.SansSerif,
                                                    color = if (item.isCorrect) Color(0xFF10B981) else Color(0xFFEF4444)
                                                )
                                            }
                                        }

                                        // If wrong, show correct option
                                        if (!item.isCorrect) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                GameText(
                                                    text = "گزینه صحیح:",
                                                    size = 12.sp,
                                                    color = if (isDark) Game.TextSecondary else Color(0xFF64748B)
                                                )

                                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                                    Text(
                                                        text = item.correctOption,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.SansSerif,
                                                        color = Color(0xFF10B981)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}
