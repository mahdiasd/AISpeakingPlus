package ir.aispeaking.search.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.sharedui.ui.core.category.CategoryDialogItem
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesDialog(
    modifier: Modifier,
    categories: ImmutableList<Category>,
    selectedCategory: Category? = null,
    onCategorySelected: (Category) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AppTheme.colors.surfaceContainerLow) {
        CategoriesDialogContent(
            modifier = modifier,
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = onCategorySelected
        )
    }
}


@Composable
private fun CategoriesDialogContent(
    modifier: Modifier,
    categories: ImmutableList<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        maxItemsInEachRow = 1,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        categories.forEach { category ->
            key(category.id) {
                CategoryDialogItem(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    item = category,
                    isSelected = selectedCategory?.id == category.id,
                    onCategoryClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun PreviewCategoriesDialog() {
    AppTheme {
        CategoriesDialogContent(
            modifier = Modifier.fillMaxWidth(),
            categories = FakeData.provideCategories(),
            selectedCategory = null,
            onCategorySelected = {},
        )
    }
}