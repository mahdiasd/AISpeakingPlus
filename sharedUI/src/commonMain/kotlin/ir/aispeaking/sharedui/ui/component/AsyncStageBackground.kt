package ir.aispeaking.sharedui.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade

@Composable
fun AsyncStageBackground(
    backgroundUrl: String?,
    modifier: Modifier = Modifier,
    dimColor: Color = Color.Black.copy(alpha = 0.45f),
    enableFrostedGlass: Boolean = true,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(targetState = backgroundUrl, label = "stage_bg_crossfade") { url ->
            if (!url.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Stage Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A1A2E),
                                    Color(0xFF16213E),
                                    Color(0xFF0F3460)
                                )
                            )
                        )
                )
            }
        }

        // Frosted glass / gradient scrim overlay
        if (enableFrostedGlass) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dimColor.copy(alpha = 0.3f),
                                dimColor.copy(alpha = 0.6f),
                                dimColor.copy(alpha = 0.85f)
                            )
                        )
                    )
            )
        }

        content()
    }
}
