package ir.aispeaking.scenarios.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.category.Category
import ir.aispeaking.sharedui.ui.core.category.CategoryItem
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import kotlinx.collections.immutable.ImmutableList

@Composable
fun CategoriesItem(
    modifier: Modifier = Modifier,
    title: String,
    categories: ImmutableList<Category>,
    onCategoryClick: (Category) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        BodyLargeBoldText(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            textAlign = TextAlign.Start
        )
        LazyRow(
            modifier = modifier
                .height(120.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            items(categories.size, key = { index -> categories[index].id })
            { index ->
                CategoryItem(
                    modifier = Modifier.animateItem(),
                    item = categories[index],
                    onCategoryClick = { onCategoryClick(categories[index]) }
                )
            }
        }
    }
}
