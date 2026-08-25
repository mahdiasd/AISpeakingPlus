package ir.aispeaking.search.component;

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.search.OnAction
import ir.aispeaking.search.SearchUiEvent
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_right
import ir.aispeaking.sharedui.is_search
import ir.aispeaking.sharedui.search_toolbar_search_box_hint
import ir.aispeaking.sharedui.search_toolbar_select_category
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.input.AppCompactTextField
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTopBar(
    onBackClick: () -> Unit,
    category: Category?,
    searchText: String,
    onAction: OnAction
) {
    ConstraintLayout(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        val (backRef, categoryRef, searchRef) = createRefs()
        AppIcon(
            modifier = Modifier
                .rotate(180f)
                .constrainAs(backRef) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    bottom.linkTo(searchRef.bottom)
                },
            icon = Res.drawable.ic_arrow_right,
            onClick = onBackClick,
        )

        AppCompactTextField(
            modifier = Modifier
                .constrainAs(searchRef)
                {
                    top.linkTo(parent.top)
                    start.linkTo(categoryRef.end, 8.dp)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .fillMaxWidth(),
            value = searchText,
            hint = stringResource(Res.string.search_toolbar_search_box_hint),
            maxLines = 1,
            trailingIcon = {
                AppIcon(icon = Res.drawable.is_search)
            },
            onFinishTyping = { onAction(SearchUiEvent.OnSearch) },

            showClearIcon = true,
            onValueChange = { onAction(SearchUiEvent.OnSearchTextChange(it)) }
        )

        Box(
            modifier = Modifier
                .constrainAs(categoryRef) {
                    top.linkTo(searchRef.top)
                    start.linkTo(backRef.end, 8.dp)
                    bottom.linkTo(searchRef.bottom)
                    height = Dimension.fillToConstraints
                }
                .background(
                    color = when (category != null) {
                        true -> AppTheme.colors.primaryContainer
                        false -> Color.Transparent
                    },
                    shape = AppTheme.shapes.roundSmall
                )
                .widthIn(min = 90.dp, max = 120.dp)
                .animateClickable { onAction(SearchUiEvent.OnCategoriesDialog(true)) }
                .border(
                    width = 1.dp,
                    color = when (category != null) {
                        true -> AppTheme.colors.primary
                        false -> AppTheme.colors.outline
                    },
                    shape = AppTheme.shapes.roundSmall
                )
                .padding(horizontal = 8.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            LabelSmallBoldText(
                modifier = Modifier,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                text = category?.name ?: stringResource(Res.string.search_toolbar_select_category),
                color = when (category != null) {
                    true -> AppTheme.colors.onPrimaryContainer
                    false -> AppTheme.colors.onSurface
                }
            )
        }
    }

}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        SearchTopBar(
            onBackClick = {},
            category = null,
            searchText = "",
            onAction = { }
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview_WithText() {
    AppTheme {
        SearchTopBar(
            onBackClick = {},
            category = null,
            searchText = "Love",
            onAction = { }
        )
    }
}