package ir.aispeaking.sharedui.ui.stage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.aispeaking.domain.model.stage.EvaluationSession

@Composable
fun FinishConversationDialog(
    evaluation: EvaluationSession,
    onContinueChatting: () -> Unit,
    onReplayStage: () -> Unit,
    onConfirmAndNext: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val stars = evaluation.calculatedStars
    val isPerfect = stars >= 3

    Dialog(onDismissRequest = onDismissRequest) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isPerfect) Color(0x6610B981) else Color(0x336366F1)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Celebration / Achievement Icon Badge
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = if (isPerfect) {
                                        listOf(Color(0xFF10B981).copy(alpha = 0.35f), Color.Transparent)
                                    } else {
                                        listOf(Color(0xFFF59E0B).copy(alpha = 0.35f), Color.Transparent)
                                    }
                                ),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isPerfect) Color(0xFF10B981) else Color(0xFFF59E0B),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPerfect) "🎉" else "🎯",
                            fontSize = 24.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Congratulations Title
                    Text(
                        text = if (isPerfect) "با موفقیت هدف این مرحله را طی کردی!" else "هدف این مرحله به پایان رسید",
                        color = if (isPerfect) Color(0xFF34D399) else Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "تعداد ستاره‌هایی که کسب کردی:",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Star Rating Display
                    StarRatingBadge(stars = stars, starSize = 36.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation Breakdown Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "نحوه محاسبه ستاره‌ها:",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💡 راهنماهای استفاده‌شده:", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                Text(
                                    text = "${evaluation.hintsUsedCount} مورد",
                                    color = if (evaluation.hintsUsedCount == 0) Color(0xFF10B981) else Color(0xFFFBBF24),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✏️ خطاهای گرامری:", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                Text(
                                    text = "${evaluation.grammarErrorsCount} مورد",
                                    color = if (evaluation.grammarErrorsCount == 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            HorizontalDivider(
                                color = Color(0xFF334155),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Clear, friendly Persian feedback explaining the star count
                            val explanationText = when (stars) {
                                3 -> "آفرین! بدون هیچ خطا یا کمکی، هر ۳ ستاره این مرحله را دریافت کردی."
                                2 -> "با ۱ مورد کسر امتیاز (خطا یا راهنما)، موفق شدی ۲ ستاره کسب کنی."
                                1 -> "با ۲ مورد کسر امتیاز (خطا یا راهنما)، ۱ ستاره دریافت کردی."
                                else -> "به دلیل ۳ خطا یا راهنما ستاره‌ای تعلق نگرفت؛ با شروع مجدد می‌توانی ستاره بگیری."
                            }

                            Text(
                                text = explanationText,
                                color = Color(0xFF93C5FD),
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Buttons Matrix based on star count
                    if (isPerfect) {
                        // 3 Stars: "ثبت و مرحله بعد" & "ادامه مکالمه"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onContinueChatting,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF94A3B8)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF475569))
                            ) {
                                Text(
                                    text = "ادامه مکالمه",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Button(
                                onClick = onConfirmAndNext,
                                modifier = Modifier
                                    .weight(1.25f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF10B981),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "ثبت و مرحله بعد",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        // < 3 Stars: "شروع مجدد" & "ثبت و مرحله بعد"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onReplayStage,
                                modifier = Modifier
                                    .weight(1.1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF59E0B),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "شروع مجدد",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onConfirmAndNext,
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFCBD5E1)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64748B))
                            ) {
                                Text(
                                    text = "ثبت و مرحله بعد",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
