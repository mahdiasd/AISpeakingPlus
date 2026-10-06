package ir.aispeaking.sharedui.ui.stage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun EvaluationResultCard(
    evaluation: EvaluationSession,
    onReplay: () -> Unit,
    onContinue: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (evaluation.calculatedStars >= 1) "🎉 آفرین! ماموریت انجام شد" else "نیاز به تمرین بیشتر",
                        color = if (evaluation.calculatedStars >= 1) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StarRatingBadge(stars = evaluation.calculatedStars, starSize = 36.sp)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Penalty Breakdown Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "محاسبه امتیاز و جریمه‌ها:",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("💡 راهنماهای استفاده‌شده:", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                Text("${evaluation.hintsUsedCount} عدد", color = Color(0xFFFBBF24), fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("✏️ خطاهای گرامری:", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                                Text("${evaluation.grammarErrorsCount} مورد", color = Color(0xFFEF4444), fontSize = 13.sp)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Divider(color = Color(0xFF334155), thickness = 0.5.dp)
                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("امتیاز کسب‌شده:", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("${evaluation.score} از ۱۰۰", color = Color(0xFF38BDF8), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Grammar Corrections
                    if (evaluation.grammarErrors.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1B2B))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "اصلاحات گرامری:",
                                    color = Color(0xFFF472B6),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                evaluation.grammarErrors.forEachIndexed { i, err ->
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        Text(
                                            text = "✗ ${err.original}",
                                            color = Color(0xFFF87171),
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = "✓ ${err.correction}",
                                            color = Color(0xFF4ADE80),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = err.explanationFa,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    if (i < evaluation.grammarErrors.size - 1) {
                                        Divider(color = Color(0xFF4A2840), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Persian Pedagogical Feedback
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF172554))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "بازخورد آموزشی:",
                                color = Color(0xFF60A5FA),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = evaluation.feedbackFa,
                                color = Color(0xFFDBEAFE),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onReplay,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8))
                        ) {
                            Text("تکرار مرحله", fontSize = 13.sp)
                        }

                        Button(
                            onClick = onContinue,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("ادامه مسیر", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
