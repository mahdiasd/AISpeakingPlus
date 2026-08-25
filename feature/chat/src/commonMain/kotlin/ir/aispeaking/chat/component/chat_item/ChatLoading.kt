package ir.aispeaking.chat.component.chat_item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.core.loading.DotLoading
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.toCorner
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun ChatLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(70.dp, 40.dp)
            .background(
                color = AppTheme.colors.aiChatContainer,
                shape = AppTheme.shapes.roundedSemiLarge.copy(bottomStart = 0.dp.toCorner())
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        DotLoading(
            modifier = Modifier.matchParentSize(),
            dotSize = 8.dp,
            horizontalArrangement = Arrangement.spacedBy(
                6.dp,
                alignment = Alignment.CenterHorizontally
            ),
            dotColor = AppTheme.colors.onSurface
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        ChatLoading()
    }
}