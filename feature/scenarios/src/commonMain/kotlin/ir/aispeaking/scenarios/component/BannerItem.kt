package ir.aispeaking.scenarios.component

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import ir.aispeaking.domain.model.banner.Banner
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.extension.animateClickable

@androidx.compose.runtime.Composable
fun BannerItem(
    modifier: Modifier = Modifier,
    banner: Banner,
    onClick: (Banner) -> Unit
) {
    AppAsyncImage(
        modifier = modifier
            .fillMaxWidth()
            .animateClickable { onClick(banner) }
            .aspectRatio(3.1f)
            ,
        contentScale = ContentScale.FillBounds,
        data = banner.bannerLink
    )
}