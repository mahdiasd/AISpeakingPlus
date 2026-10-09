package ir.aispeaking.profile.component

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameSheet
import ir.aispeaking.sharedui.ui.game.GameText
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
        accentColor = Game.Sky
    ),
    CefrLevelInfo(
        code = "A2",
        titleFa = "پیش‌متوسط (Elementary)",
        descriptionFa = "درک عبارات پرکاربرد و مکالمات ساده پیرامون کارهای روزانه",
        accentColor = Game.Mint
    ),
    CefrLevelInfo(
        code = "B1",
        titleFa = "متوسط (Intermediate)",
        descriptionFa = "مکالمه در موقعیت‌های سفر، کار و بیان تجربیات و اهداف",
        accentColor = Game.Blue
    ),
    CefrLevelInfo(
        code = "B2",
        titleFa = "فوق‌متوسط (Upper-Intermediate)",
        descriptionFa = "صحبت روان و موثر در بحث‌های فنی، اجتماعی و تخصصی",
        accentColor = Game.Violet
    ),
    CefrLevelInfo(
        code = "C1",
        titleFa = "پیشرفته (Advanced)",
        descriptionFa = "درک آسان مفاهیم پیچیده و کاربرد منعطف و خلاقانه زبان",
        accentColor = Game.Gold
    ),
    CefrLevelInfo(
        code = "C2",
        titleFa = "مسلط / بومی (Mastery)",
        descriptionFa = "تسلط کامل، دقیق، سریع و بی‌وقفه در تمامی ابعاد زبان",
        accentColor = Game.GoldDeep
    )
)

@Composable
fun LevelPickerBottomSheet(
    visible: Boolean = true,
    currentLevel: String?,
    onLevelSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLevelCode = (currentLevel?.takeIf { it.isNotBlank() } ?: "A1").uppercase()

    GameSheet(visible = visible, onDismiss = onDismiss, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GameText(text = "سطح زبانت چیست؟", size = 18.sp, bold = true, align = TextAlign.Center)
            GameText(
                text = "سطح انگلیسی خودت را انتخاب کن؛ بعداً هم می‌توانی تغییرش بدهی.",
                size = 12.sp,
                color = Game.TextSecondary,
                align = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(CEFR_LEVELS, key = { it.code }) { levelInfo ->
                    val isSelected = levelInfo.code.equals(activeLevelCode, ignoreCase = true)
                    LevelOptionCard(
                        levelInfo = levelInfo,
                        isSelected = isSelected,
                        onClick = { onLevelSelected(levelInfo.code) }
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
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = 450f),
        label = "level_option_press"
    )

    val cardBg = if (isSelected) Game.PanelRaised else Game.PanelSolid
    val cardBorder = if (isSelected) levelInfo.accentColor else Game.Stroke
    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(cardBg)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = cardBorder,
                shape = shape
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 13.dp)
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
                    .background(levelInfo.accentColor.copy(alpha = if (isSelected) 0.22f else 0.12f))
                    .border(
                        width = 1.dp,
                        color = levelInfo.accentColor.copy(alpha = if (isSelected) 0.75f else 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                GameText(
                    text = levelInfo.code,
                    color = levelInfo.accentColor,
                    size = 15.sp,
                    bold = true,
                    latin = true
                )
            }

            // Title & Description
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                GameText(
                    text = levelInfo.titleFa,
                    color = if (isSelected) Game.TextPrimary else Game.TextSecondary,
                    size = 14.sp,
                    bold = isSelected
                )
                GameText(
                    text = levelInfo.descriptionFa,
                    color = Game.TextSecondary,
                    size = 11.sp,
                    lineHeight = 16.sp
                )
            }

            // Radio / Checkmark Selection Indicator
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
                        .border(1.5.dp, Game.StrokeStrong, CircleShape)
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
