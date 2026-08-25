package ir.aispeaking.sharedui.ui.core.tab

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag


import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme

import ir.aispeaking.sharedui.ui.core.text.TitleBoldText


import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.ui.extension.baseModifier
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.tab.UiTab
import ir.aispeaking.sharedui.*
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppTab(
    modifier: Modifier = Modifier.fillMaxWidth(),
    items: ImmutableList<UiTab>,
    onClick: (UiTab) -> Unit,
    unSelectedContainerColor: Color = AppTheme.colors.onSurface,
    unSelectedTextColor: Color = AppTheme.colors.onSurface,
    selectedContainerColor: Color = AppTheme.colors.primary,
    selectedTextColor: Color = AppTheme.colors.onPrimary,
    currentTab: UiTab
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        items(items = items, key = { it.title.key })
        { uiTab ->
            uiTab.vector?.let { vector ->
                Column(
                    modifier = Modifier
                        .testTag("tab-${uiTab.title}")
                        .animateClickable { onClick(uiTab) }
                        .widthIn(min = 110.dp)
                        .background(
                            if (uiTab == currentTab) selectedContainerColor else unSelectedContainerColor,
                            shape = AppTheme.shapes.roundLarge
                        )
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        16.dp,
                        alignment = Alignment.CenterVertically
                    )
                ) {
                    Image(
                        modifier = Modifier.size(75.dp),
                        painter = painterResource(vector),
                        contentDescription = stringResource(uiTab.title),
                    )
                    TitleBoldText(
                        modifier = Modifier,
                        color = if (uiTab == currentTab) selectedTextColor else unSelectedTextColor,
                        textAlign = TextAlign.Center,
                        text = stringResource(uiTab.title)
                    )
                }
            } ?: run {
                TitleBoldText(
                    modifier = Modifier
                        .testTag("tab-${uiTab.title}")
                        .animateClickable { onClick(uiTab) }
                        .widthIn(min = 110.dp)
                        .background(
                            if (uiTab == currentTab) selectedContainerColor else unSelectedContainerColor,
                            shape = AppTheme.shapes.roundLarge
                        )
                        .padding(vertical = 8.dp),
                    color = if (uiTab == currentTab) selectedTextColor else unSelectedTextColor,
                    textAlign = TextAlign.Center,
                    text = stringResource(uiTab.title)
                )
            }
        }
    }
}

@Composable
fun AppVerticalTab(
    modifier: Modifier = Modifier,
    items: ImmutableList<UiTab>,
    onClick: (UiTab) -> Unit,
    unSelectedContainerColor: Color,
    unSelectedTextColor: Color,
    selectedContainerColor: Color = AppTheme.colors.primary,
    selectedTextColor: Color = AppTheme.colors.onPrimary,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(16.dp),
    currentTab: UiTab
) {
    LazyColumn(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        items(items = items, key = { it.title.key })
        { uiTab ->
            TitleBoldText(
                modifier = Modifier
                    .testTag("tab-${uiTab.title}")
                    .animateClickable { onClick(uiTab) }
                    .widthIn(min = 110.dp)
                    .background(
                        if (uiTab == currentTab) selectedContainerColor else unSelectedContainerColor,
                        shape = AppTheme.shapes.roundLarge
                    )
                    .padding(vertical = 8.dp),
                color = if (uiTab == currentTab) selectedTextColor else unSelectedTextColor,
                textAlign = TextAlign.Center,
                text = stringResource(uiTab.title)
            )
        }
    }
}


@PreviewLightDark
@Composable
private fun Preview() {
    AppTheme {
        Box(modifier = Modifier.baseModifier()) {
            AppTab(
                items = immutableListOf(PreviewUiTab.Completed(), PreviewUiTab.Canceled()),
                onClick = {},
                currentTab = PreviewUiTab.Completed(),
                unSelectedContainerColor = AppTheme.colors.onSurface,
                unSelectedTextColor = AppTheme.colors.onSurface,
                selectedContainerColor = AppTheme.colors.primary,
                selectedTextColor = AppTheme.colors.primary
            )
        }
    }
}

private sealed class PreviewUiTab : UiTab {
    data class Completed(
        override val title: StringResource = Res.string.tab_tile_completed,
        override val vector: DrawableResource? = null
    ) : PreviewUiTab()

    data class Canceled(
        override val title: StringResource = Res.string.tab_tile_canceled,
        override val vector: DrawableResource? = null
    ) : PreviewUiTab()
}

