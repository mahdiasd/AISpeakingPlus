package ir.aispeaking.leitner.dialog

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_bell
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameButton
import ir.aispeaking.sharedui.ui.game.GameButtonStyle
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.game.fa
import ir.aispeaking.sharedui.ui.them.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeitnerReminderSheet(
    initialHour: Int,
    initialMinute: Int,
    onSave: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = AppTheme.isDark
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var hour by remember(initialHour) { mutableStateOf(initialHour.coerceIn(0, 23)) }
    var minute by remember(initialMinute) { mutableStateOf((initialMinute / 5 * 5).coerceIn(0, 55)) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = if (isDark) Color(0xF5131620) else Color(0xFAF8FAFC),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            tonalElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF06B6D4).copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        painter = org.jetbrains.compose.resources.painterResource(Res.drawable.ic_bell),
                        contentDescription = null,
                        tint = Color(0xFF06B6D4),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GameText(
                        text = "تنظیم ساعت یادآور روزانه",
                        size = 18.sp,
                        bold = true,
                        color = Game.TextPrimary,
                        align = TextAlign.Center
                    )

                    GameText(
                        text = "برای اینکه کارت‌های لایتنرت انباشته نشن، در این ساعت بهت یادآوری می‌کنیم.",
                        size = 13.sp,
                        color = Game.TextSecondary,
                        align = TextAlign.Center
                    )
                }

                // Quick Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    TimePresetChip(
                        title = "صبح (۰۹:۰۰)",
                        selected = hour == 9 && minute == 0,
                        onClick = { hour = 9; minute = 0 }
                    )
                    TimePresetChip(
                        title = "عصر (۱۸:۰۰)",
                        selected = hour == 18 && minute == 0,
                        onClick = { hour = 18; minute = 0 }
                    )
                    TimePresetChip(
                        title = "شب (۲۱:۰۰)",
                        selected = hour == 21 && minute == 0,
                        onClick = { hour = 21; minute = 0 }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Numeric Stepper Row (No Keyboard!)
                // In RTL layout: Right is Hour, Left is Minute
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color(0x24FFFFFF) else Color(0xCCFFFFFF))
                        .border(1.dp, Game.Stroke, RoundedCornerShape(24.dp))
                        .padding(vertical = 16.dp, horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour Stepper
                    StepperColumn(
                        label = "ساعت",
                        value = hour,
                        onIncrement = { hour = (hour + 1) % 24 },
                        onDecrement = { hour = if (hour - 1 < 0) 23 else hour - 1 }
                    )

                    // Colon Divider
                    GameText(
                        text = ":",
                        size = 36.sp,
                        bold = true,
                        color = Color(0xFF06B6D4),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Minute Stepper (5 min steps)
                    StepperColumn(
                        label = "دقیقه",
                        value = minute,
                        onIncrement = { minute = (minute + 5) % 60 },
                        onDecrement = { minute = if (minute - 5 < 0) 55 else minute - 5 }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Save Confirmation Button
                GameButton(
                    text = "ذخیره و فعال‌سازی یادآور",
                    onClick = {
                        onSave(hour, minute)
                    },
                    style = GameButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    height = 52.dp,
                    textSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun StepperColumn(
    label: String,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GameText(
            text = label,
            size = 12.sp,
            color = Game.TextSecondary
        )

        // Increment Button (+)
        StepperButton(
            text = "+",
            onClick = onIncrement
        )

        // Number Display
        Box(
            modifier = Modifier
                .width(70.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (AppTheme.isDark) Color(0x33000000) else Color(0x1A000000)),
            contentAlignment = Alignment.Center
        ) {
            GameText(
                text = value.toString().padStart(2, '0').fa(),
                size = 32.sp,
                bold = true,
                color = Game.TextPrimary,
                align = TextAlign.Center
            )
        }

        // Decrement Button (-)
        StepperButton(
            text = "−",
            onClick = onDecrement
        )
    }
}

@Composable
private fun StepperButton(
    text: String,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val isDark = AppTheme.isDark

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow),
        label = "stepper_btn_scale"
    )

    Box(
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(if (isDark) Color(0x33FFFFFF) else Color(0x1F000000))
            .border(1.dp, Game.Stroke, CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        GameText(
            text = text,
            size = 22.sp,
            bold = true,
            color = Game.TextPrimary,
            align = TextAlign.Center
        )
    }
}

@Composable
private fun TimePresetChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val isDark = AppTheme.isDark
    val accent = Color(0xFF06B6D4)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.22f) else if (isDark) Color(0x1AFFFFFF) else Color(0x14000000))
            .border(if (selected) 1.5.dp else 1.dp, if (selected) accent else Game.Stroke, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        GameText(
            text = title.fa(),
            size = 11.sp,
            bold = selected,
            color = if (selected) accent else Game.TextSecondary
        )
    }
}
