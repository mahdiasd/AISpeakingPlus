package ir.aispeaking.subscription.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.aispeaking.sharedui.ui.core.button.AppBackButton
import ir.aispeaking.sharedui.ui.game.GameText
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun ModernSubscriptionHeader(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = AppTheme.isDark

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Unified Top Navigation Bar: Back button on RTL start (Right), centered title, balanced spacer on left
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppBackButton(
                onClick = onNavigateBack,
                containerSize = 38.dp,
                iconSize = 18.dp,
                backgroundColor = if (isDark) Color(0x33FFFFFF) else Color(0x99FFFFFF),
                borderColor = if (isDark) Color(0x38FFFFFF) else Color(0x1F000000)
            )

            GameText(
                text = "تعرفه‌های اشتراک",
                size = 18.sp,
                bold = true,
                color = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A),
                align = TextAlign.Center
            )

            // Symmetrical balance spacer matching back button size
            Spacer(modifier = Modifier.size(38.dp))
        }
    }
}
