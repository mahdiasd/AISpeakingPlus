package ir.aispeaking.sharedui.ui.core.loading

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun PageLoading(
    modifier: Modifier = Modifier.fillMaxSize(),
    dotSize: Dp = 22.dp,
    dotColor: Color = AppTheme.colors.onSurface,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    )
    {
        DotLoading(
            modifier = Modifier,
            dotSize = dotSize,
            dotColor = dotColor
        )
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        PageLoading(
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}