package ir.aispeaking.sharedui.ui.core.guide

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.absoluteValue

@Composable
fun ModernImageSlider(
    modifier: Modifier = Modifier,
    imageSources: List<DrawableResource>,
    imageCornerRadius: Dp = 16.dp,
    slideInAnimation: Boolean = true,
) {
    val pagerState = rememberPagerState(pageCount = { imageSources.size })

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 32.dp)
        ) { page ->
            ImageCard(
                imageSource = imageSources[page],
                pagerState = pagerState,
                page = page,
                cornerRadius = imageCornerRadius,
                applyAnimation = slideInAnimation
            )
        }

        PagerIndicator(
            modifier = Modifier
                .padding(bottom = 16.dp),
            pagerState = pagerState
        )
    }
}

@Composable
private fun ImageCard(
    imageSource: DrawableResource,
    pagerState: PagerState,
    page: Int,
    cornerRadius: Dp,
    applyAnimation: Boolean,
) {
    Card(
        shape = RoundedCornerShape(cornerRadius),
        modifier = Modifier
            .aspectRatio(0.5354223f)
            .graphicsLayer {
                if (applyAnimation) {
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    val scale = lerp(
                        start = 0.85f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                    scaleX = scale
                    scaleY = scale

                    alpha = lerp(
                        start = 0.5f,
                        stop = 1f,
                        fraction = 1f - pageOffset.coerceIn(0f, 1f)
                    )
                }
            }
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.5354223f),
            painter = painterResource(imageSource),
            contentDescription = "Slider Image",
        )
    }
}


@Composable
fun PagerIndicator(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    activeColor: Color = AppTheme.colors.primary,
    inactiveColor: Color = AppTheme.colors.outline,
    activeWidth: Dp = 16.dp,
    inactiveSize: Dp = 8.dp,
    indicatorHeight: Dp = 8.dp,
    spacing: Dp = 8.dp,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(pagerState.pageCount) { index ->
            val isSelected = pagerState.currentPage == index

            Box(
                modifier = Modifier
                    .size(
                        width = if (isSelected) activeWidth else inactiveSize,
                        height = indicatorHeight
                    )
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else inactiveColor)
            )
        }
    }
}
