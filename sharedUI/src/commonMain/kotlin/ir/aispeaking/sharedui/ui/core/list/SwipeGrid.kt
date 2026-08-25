package ir.aispeaking.sharedui.ui.core.list

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.core.empty.EmptyContent
import ir.aispeaking.sharedui.ui.core.error.ErrorContent
import ir.aispeaking.sharedui.ui.extension.OnBottomReached
import ir.aispeaking.sharedui.ui.them.AppTheme


@ExperimentalMaterial3Api
@Composable
fun SwipeGrid(
    modifier: Modifier,
    isRefreshing: Boolean,
    isLoadMore: Boolean,
    boxState: PullToRefreshState = rememberPullToRefreshState(),
    lazyState: LazyGridState = rememberLazyGridState(),
    listSize: Int?,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    key: ((Int) -> Any)?,
    contentPadding: PaddingValues = PaddingValues(bottom = 48.dp),

    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(
        16.dp,
        alignment = Alignment.CenterVertically
    ),
    horizontalAlignment: Arrangement.Horizontal = Arrangement.Start,

    errorHappened: Boolean = false,

    contentAlignment: Alignment = Alignment.TopStart,

    itemAnimation: Boolean = false,
    columns: GridCells = GridCells.Fixed(3),

    screenToShow: @Composable (index: Int, modifier: Modifier) -> Unit,
) {
    val showEmptyVector by remember(isRefreshing, isLoadMore, listSize) {
        derivedStateOf {
            ((!isLoadMore && !isRefreshing) && listSize != null && listSize == 0 && !errorHappened)
        }
    }

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = false,
        state = boxState,
        contentAlignment = contentAlignment,
        onRefresh = onRefresh,
        indicator = {
            if (isRefreshing)
                CircularProgressIndicator(
                    modifier = Modifier
                        .testTag("load-more-icon")
                        .align(Alignment.TopCenter)
                        .size(size = 42.dp)
                        .padding(4.dp)
                        .shadow(2.dp, shape = CircleShape, spotColor = AppTheme.colors.primary)
                        .background(AppTheme.colors.onPrimary, shape = CircleShape)
                        .padding(4.dp),
                    strokeWidth = 3.dp,
                    color = AppTheme.colors.primary
                )
        }
    ) {
        if (showEmptyVector) {
            EmptyContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            )
        }

        LazyVerticalGrid(
            modifier = Modifier
                .clipScrollableContainer(Orientation.Vertical)
                .align(contentAlignment),
            state = lazyState,
            contentPadding = contentPadding,
            columns = columns,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalAlignment,
            userScrollEnabled = true
        ) {
            items(listSize ?: 0, key = key) { index ->
                if (itemAnimation)
                    screenToShow(
                        index,
                        Modifier.animateItem(
                            fadeInSpec = tween(
                                500,
                                delayMillis = index * 100,
                                easing = LinearOutSlowInEasing
                            )
                        )
                    )
                else
                    screenToShow(index, Modifier)
            }
        }

        if (isLoadMore) {
            CircularProgressIndicator(
                modifier = Modifier
                    .testTag("load-more-icon")
                    .align(Alignment.BottomCenter)
                    .size(size = 32.dp)
                    .padding(4.dp)
                    .shadow(2.dp, shape = CircleShape, spotColor = AppTheme.colors.primary)
                    .background(Color.White, shape = CircleShape)
                    .padding(bottom = 8.dp),
                strokeWidth = 3.dp,
                color = AppTheme.colors.primary
            )
        }


        if (errorHappened) {
            ErrorContent(
                modifier = Modifier
                    .align(Alignment.Center),
                onRetry = onRefresh
            )
        }
    }

    lazyState.OnBottomReached {
        onLoadMore.invoke()
    }
}