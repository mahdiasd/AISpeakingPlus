package ir.aispeaking.main.component

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.them.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreakDetailDialog(
    streakDays: Int = 5,
    onDismiss: () -> Unit,
) {
    val weekDays = listOf(
        Pair("ش", true),   // شنبه
        Pair("ی", true),   // یکشنبه
        Pair("د", true),   // دوشنبه
        Pair("س", true),   // سه‌شنبه
        Pair("چ", true),   // چهارشنبه
        Pair("پ", false),  // پنج‌شنبه
        Pair("ج", false),  // جمعه
    )

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFFFF9800))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A),
                            Color(0xFF080D1A),
                        ),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFF9800), Color(0x60FF5722), Color(0x20334155)),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Vector Flame Icon Header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFF9800), Color(0xFFE65100), Color(0x00000000)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    GameFlameIcon(size = 44.dp)
                }

                // Title
                Text(
                    text = "استریک $streakDays روزه تمرین!",
                    color = Color(0xFFFFE082),
                    fontSize = 20.sp,
                    fontFamily = AppTheme.typography.persianBold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = TextDirection.Rtl,
                    ),
                )

                // Subtitle explanation
                Text(
                    text = "آفرین قهرمان! با ۵ روز تمرین پیاپی، زنجیره یادگیری‌ات فعاله و پاداش الماس‌ها ۲ برابر محاسبه میشه.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontFamily = AppTheme.typography.persianRegular,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = TextDirection.Rtl,
                    ),
                )

                // 7-day tracker row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x60020617))
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    weekDays.forEach { (dayName, isCompleted) ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = dayName,
                                color = if (isCompleted) Color(0xFFFFCA28) else Color(0xFF64748B),
                                fontSize = 12.sp,
                                fontFamily = AppTheme.typography.persianBold,
                                fontWeight = FontWeight.Bold,
                            )

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isCompleted)
                                            Brush.radialGradient(listOf(Color(0xFFFF9800), Color(0xFFE65100)))
                                        else
                                            Brush.radialGradient(listOf(Color(0xFF334155), Color(0xFF1E293B))),
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isCompleted) Color(0xFFFFE082) else Color(0xFF475569),
                                        shape = CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (isCompleted) {
                                    GameFlameIcon(size = 18.dp, animated = false)
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF64748B)),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Close Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFFB300), Color(0xFFFF8F00)),
                            ),
                        )
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "بزن بریم ادامه بدیم!",
                        color = Color(0xFF0F172A),
                        fontFamily = AppTheme.typography.persianBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}
