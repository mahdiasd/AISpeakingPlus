package ir.aispeaking.sharedui.ui.core.horizontal_pager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.ui.permission.AudioPermission
import ir.aispeaking.sharedui.permission.PostNotificationPermission
import ir.aispeaking.sharedui.ui.core.permission.PermissionItem
import ir.aispeaking.sharedui.ui.core.space.HorizontalSpace
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.them.AppTheme


@Composable
fun AppHorizontalPager(
    modifier: Modifier = Modifier,
    itemCount: Int,
    key: ((index: Int) -> Any)?,
    userScrollEnabled: Boolean = true,
    reverseLayout: Boolean = false,
    pageSpacing: Dp = 16.dp,
    showScrollIcons: Boolean = itemCount > 1,
    content: @Composable (index: Int, modifier: Modifier) -> Unit,
) {
    val pageCount by remember(itemCount) { mutableIntStateOf(itemCount + 2) }
    val state = rememberPagerState(pageCount = { pageCount })

    LaunchedEffect(Unit) {
        state.scrollToPage(0)
    }

    val centerModifier = Modifier
        .alpha(1f)
        .coloredShadow(
            color = AppTheme.colors.primary,
            borderRadius = 10.dp,
            shadowRadius = 10.dp,
            offsetY = 2.dp,
            alpha = 0.6f
        )
        .shadow(2.dp, shape = AppTheme.shapes.roundMedium)
        .background(AppTheme.colors.onSurface, shape = AppTheme.shapes.roundMedium)
        .padding(0.dp)
        .fillMaxHeight()

    val insidesModifier = Modifier
        .alpha(0.4f)
        .background(AppTheme.colors.onSurface, shape = AppTheme.shapes.roundMedium)
        .padding(8.dp)
        .fillMaxHeight(0.8f)

    HorizontalPager(
        state = state,
        modifier = modifier,
        pageSize = PageSize.Fixed(250.dp),
        pageSpacing = pageSpacing,
        verticalAlignment = Alignment.CenterVertically,
        userScrollEnabled = userScrollEnabled,
        reverseLayout = reverseLayout,
        key = { page ->
            when (page) {
                0 -> "fake_start"
                pageCount - 1 -> "fake_end"
                else -> key?.invoke(page - 1) ?: page
            }
        },
    ) { page ->
        when (page) {
            0 -> {
                HorizontalSpace(250.dp)
            }
            pageCount - 1 -> {
                HorizontalSpace(250.dp)
            }
            else -> content(page - 1, if (state.currentPage == page - 1) centerModifier else insidesModifier)
        }
    }

}


/**
 * Find center position of horizontal pager */
private fun findCenterPosition(pageCount: Int): Int {
    val count = pageCount + 2
    return if (count <= 1) 0
    else if (count % 2 == 0) (count / 2) - 1
    else (count / 2)
}

@Preview(name = "Landscape Mode", showBackground = true, widthDp = 720, heightDp = 350)
@Composable
private fun Preview() {
    val permissions = immutableListOf(AudioPermission(), PostNotificationPermission())
    AppTheme {
        AppHorizontalPager(
            modifier = Modifier
                .fillMaxWidth(),
            itemCount = permissions.size,
            key = { permissions[it].permission },
            content = { index, modifier ->
                PermissionItem(
                    modifier = Modifier
                        .width(width = 250.dp)
                        .then(modifier),
                    appPermission = permissions[index],
                    addSpace = index != permissions.size - 1
                )
            }
        )
    }
}