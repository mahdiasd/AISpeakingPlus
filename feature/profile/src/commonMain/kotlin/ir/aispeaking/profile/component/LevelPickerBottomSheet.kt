package ir.aispeaking.profile.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import org.jetbrains.compose.resources.painterResource

data class CefrLevelInfo(
    val code: String,
    val titleFa: String,
    val descriptionFa: String,
    val accentColor: Color
)

private val CEFR_LEVELS = listOf(
    CefrLevelInfo(
        code = "A1",
        titleFa = "مبتدی (Beginner)",
        descriptionFa = "آشنایی با کلمات ساده، معرفی خود و اصطلاحات روزمره",
        accentColor = Color(0xFF38BDF8)
    ),
    CefrLevelInfo(
        code = "A2",
        titleFa = "پیش‌متوسط (Elementary)",
        descriptionFa = "درک عبارات پرکاربرد و مکالمات ساده پیرامون کارهای روزانه",
        accentColor = Color(0xFF34D399)
    ),
    CefrLevelInfo(
        code = "B1",
        titleFa = "متوسط (Intermediate)",
        descriptionFa = "مکالمه در موقعیت‌های سفر، کار و بیان تجربیات و اهداف",
        accentColor = Color(0xFF818CF8)
    ),
    CefrLevelInfo(
        code = "B2",
        titleFa = "فوق‌متوسط (Upper-Intermediate)",
        descriptionFa = "صحبت روان و موثر در بحث‌های فنی، اجتماعی و تخصصی",
        accentColor = Color(0xFFA78BFA)
    ),
    CefrLevelInfo(
        code = "C1",
        titleFa = "پیشرفته (Advanced)",
        descriptionFa = "درک آسان مفاهیم پیچیده و کاربرد منعطف و خلاقانه زبان",
        accentColor = Color(0xFFF59E0B)
    ),
    CefrLevelInfo(
        code = "C2",
        titleFa = "مسلط / بومی (Mastery)",
        descriptionFa = "تسلط کامل، دقیق، سریع و بی‌وقفه در تمامی ابعاد زبان",
        accentColor = Color(0xFFFFD700)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelPickerBottomSheet(
    currentLevel: String?,
    onLevelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLevelCode = (currentLevel?.takeIf { it.isNotBlank() } ?: "A1").uppercase()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF0F172A),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = Color(0xFF475569)
            )
        },
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "انتخاب سطح زبان",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "سطح تسلط خود به زبان انگلیسی را مشخص کنید",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(CEFR_LEVELS, key = { it.code }) { levelInfo ->
                    val isSelected = levelInfo.code.equals(activeLevelCode, ignoreCase = true)
                    LevelOptionCard(
                        levelInfo = levelInfo,
                        isSelected = isSelected,
                        onClick = {
                            onLevelSelected(levelInfo.code)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelOptionCard(
    levelInfo: CefrLevelInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isSelected) Color(0xFF1E293B) else Color(0xFF131D33)
    val cardBorder = if (isSelected) levelInfo.accentColor else Color(0x226366F1)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = cardBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level badge code (e.g. A1, B2)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(levelInfo.accentColor.copy(alpha = if (isSelected) 0.25f else 0.15f))
                    .border(
                        width = 1.dp,
                        color = levelInfo.accentColor.copy(alpha = if (isSelected) 0.8f else 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = levelInfo.code,
                    color = levelInfo.accentColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Title & Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = levelInfo.titleFa,
                    color = if (isSelected) Color.White else Color(0xFFE2E8F0),
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
                Text(
                    text = levelInfo.descriptionFa,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }

            // Radio/Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(levelInfo.accentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_done),
                        contentDescription = "Selected",
                        tint = Color.Black,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF475569), CircleShape)
                )
            }
        }
    }
}

/* --------------------------------- Previews --------------------------------- */

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun LevelPickerBottomSheetPreview() {
    ir.aispeaking.sharedui.ui.them.AppTheme {
        LevelPickerBottomSheet(
            currentLevel = "B1",
            onLevelSelected = {},
            onDismiss = {}
        )
    }
}
