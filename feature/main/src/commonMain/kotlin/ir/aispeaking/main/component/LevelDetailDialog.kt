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
fun LevelDetailDialog(
    level: Int = 3,
    currentXp: Int = 750,
    maxXp: Int = 1000,
    onDismiss: () -> Unit,
) {
    val progress = (currentXp.toFloat() / maxXp.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    val remainingXp = (maxXp - currentXp).coerceAtLeast(0)

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp), spotColor = Color(0xFFFFD54F))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E293B),
                            Color(0xFF0F172A),
                            Color(0xFF060B14),
                        ),
                    ),
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFD54F), Color(0x6000E5FF), Color(0x20334155)),
                    ),
                    shape = RoundedCornerShape(28.dp),
                )
                .padding(24.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Vector Golden Star Icon Header
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFB300), Color(0x00000000)),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    GameStarIcon(size = 46.dp)
                }

                // Level Title
                Text(
                    text = "سطح $level: کاوشگر ماجراجو",
                    color = Color(0xFFFFE082),
                    fontSize = 20.sp,
                    fontFamily = AppTheme.typography.persianBold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = TextDirection.Rtl,
                    ),
                )

                // Subtitle Progress
                Text(
                    text = "فقط $remainingXp امتیاز XP دیگه تا رسیدن به سطح ${level + 1} و دریافت ۲۰ الماس پاداش باقی مونده!",
                    color = Color(0xFFCBD5E1),
                    fontSize = 13.sp,
                    fontFamily = AppTheme.typography.persianRegular,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    style = androidx.compose.ui.text.TextStyle(
                        textDirection = TextDirection.Rtl,
                    ),
                )

                // XP Progress Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x60020617))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "پیشرفت سطح",
                            color = Color(0xFF94A3B8),
                            fontFamily = AppTheme.typography.persianRegular,
                            fontSize = 12.sp,
                        )
                        Text(
                            text = "$currentXp / $maxXp XP",
                            color = Color(0xFFFFE082),
                            fontFamily = AppTheme.typography.persianBold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF1E293B)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            Color(0xFFFFB300),
                                            Color(0xFFFFD54F),
                                            Color(0xFF00E5FF),
                                        ),
                                    ),
                                ),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dismiss Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF00E5FF), Color(0xFF0288D1)),
                            ),
                        )
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "عالیه!",
                        color = Color.White,
                        fontFamily = AppTheme.typography.persianBold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                    )
                }
            }
        }
    }
}
