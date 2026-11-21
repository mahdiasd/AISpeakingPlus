package ir.aispeaking.leitner.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_bell
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.game.Game
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource

@Composable
fun LeitnerTopBar(
    onNavigateBack: () -> Unit,
    onReminderClick: () -> Unit,
    hasActiveReminder: Boolean,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val bellScale = if (isPressed) 0.92f else 1f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Start Anchor (RTL: Top Right) - Mandatory AppBackButton
        Box(modifier = Modifier.align(Alignment.CenterStart)) {
            AppBackButton(onClick = onNavigateBack)
        }

        // Symmetrical Centered Title
        GameText(
            text = "جعبه لایتنر",
            size = 18.sp,
            bold = true,
            color = Game.TextPrimary,
            align = TextAlign.Center
        )

        // End Anchor (RTL: Top Left) - Reminder Bell
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(40.dp)
                .scale(bellScale)
                .clip(CircleShape)
                .background(if (isDark) Color(0x24FFFFFF) else Color(0xCCFFFFFF))
                .border(1.dp, Game.Stroke, CircleShape)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    onClick = onReminderClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_bell),
                contentDescription = "تنظیم یادآور",
                tint = if (hasActiveReminder) Color(0xFF06B6D4) else Game.TextPrimary,
                modifier = Modifier.size(19.dp)
            )

            if (hasActiveReminder) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 3.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF06B6D4))
                        .border(1.5.dp, if (isDark) Color(0xFF0F172A) else Color.White, CircleShape)
                )
            }
        }
    }
}
