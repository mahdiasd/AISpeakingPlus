package ir.aispeaking.sharedui.ui.core.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.size.Size
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.utils.dLog

sealed class ImageState {
    data object Loading : ImageState()
    data object Success : ImageState()
    data class Error(val message: String) : ImageState()
}

@Composable
fun AppAsyncImage(
    modifier: Modifier = Modifier,
    data: String?,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    shadow: Dp = 0.dp,
    shadowColor: Color = AppTheme.colors.shadow,
    contentDescription: String? = null,
    placeholderColor: Color = AppTheme.colors.surfaceContainer,
    contentScale: ContentScale = ContentScale.FillBounds
) {
    data.dLog(tag = "Coil3", plusTag = "data")
    var imageState: ImageState by remember { mutableStateOf(ImageState.Loading) }

    val imageRequest = ImageRequest.Builder(LocalPlatformContext.current)
        .data(data)
        .memoryCacheKey(data)
        .diskCacheKey(data)
        .size(Size.ORIGINAL)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .networkCachePolicy(CachePolicy.ENABLED)
        .crossfade(true)
        .build()

    SubcomposeAsyncImage(
        model = imageRequest,
        modifier = modifier
            .then(
                if (imageState != ImageState.Loading)
                    Modifier.shadow(
                        elevation = shadow,
                        shape = shape,
                        ambientColor = shadowColor,
                        spotColor = shadowColor
                    )
                else Modifier
            )
            .clip(shape),
        contentScale = contentScale,
        error = {
            it.result.throwable.message.dLog(tag = "Coil3", plusTag = "error")
            imageState = ImageState.Error(it.result.throwable.message ?: "")
            Spacer(
                modifier = modifier.background(color = placeholderColor, shape = shape)
            )
        },
        loading = {
            it.dLog(tag = "Coil3", plusTag = "loading")
            imageState = ImageState.Loading
            Box(modifier = Modifier.matchParentSize()) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                    color = AppTheme.colors.primary,
                    strokeWidth = 2.dp
                )
            }
        },
        onSuccess = { imageState = ImageState.Success },
        contentDescription = contentDescription
    )
}

