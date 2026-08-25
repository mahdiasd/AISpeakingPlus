package ir.aispeaking.sharedui.ui.core.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*
import ir.aispeaking.sharedui.*

import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme


@Composable
fun CategoryItem(
    modifier: Modifier,
    item: Category,
    onCategoryClick: () -> Unit
) {
    Column(
        modifier = modifier
            .animateClickable(onCategoryClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            8.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        item.imageUrl?.let {
            AppAsyncImage(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .aspectRatio(1f),
                data = it,
                shape = AppTheme.shapes.roundSmall,
                shadow = 5.dp,
                contentDescription = "banner"
            )
        } ?: run {
            Spacer(
                modifier = modifier
                    .size(85.dp)
                    .background(
                        color = AppTheme.colors.secondary,
                        shape = AppTheme.shapes.roundMedium
                    ),
            )
        }

        LabelSmallBoldText(text = item.name)
    }
}


@Composable
fun CategoryDialogItem(
    modifier: Modifier,
    item: Category,
    isSelected: Boolean,
    onCategoryClick: () -> Unit
) {
    Row(
        modifier = modifier
            .animateClickable(onCategoryClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppAsyncImage(
            modifier = Modifier
                .size(65.dp),
            data = item.imageUrl,
            shape = AppTheme.shapes.roundMedium,
            shadow = 5.dp,
            contentDescription = "${item.name} image"
        )

        BodyMediumBoldText(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Start,
            text = item.name,
            maxLines = 1,
            color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.onSurface,
            overflow = TextOverflow.MiddleEllipsis
        )

        if (isSelected) {
            AppIcon(
                modifier = Modifier
                    .shadow(1.dp, shape = CircleShape)
                    .background(AppTheme.colors.surfaceContainerLowest, shape = CircleShape)
                    .padding(4.dp),
                icon = Res.drawable.ic_done,
                size = 24.dp,
                tint = AppTheme.colors.success
            )
        }
    }
}


@LightDarkPreview
@Composable
fun PreviewCategoryItem() {
    AppTheme {
        CategoryItem(
            modifier = Modifier,
            item = FakeData.provideCategories().first(),
            onCategoryClick = {}
        )
    }
}

@LightDarkPreview
@Composable
fun PreviewCategoryDialogItem() {
    AppTheme {
        CategoryDialogItem(
            modifier = Modifier,
            item = FakeData.provideCategories().first(),
            isSelected = true,
            onCategoryClick = {}
        )
    }
}
