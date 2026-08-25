package ir.aispeaking.sharedui.ui.core.scenario

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme

@Composable
fun ScenarioSummaryItem(
    modifier: Modifier = Modifier,
    item: ScenarioSummary,
    onScenarioClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .animateClickable(onScenarioClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            4.dp,
            alignment = Alignment.CenterVertically
        )
    ) {
        var imageWidthPx by remember { mutableIntStateOf(0) }
        val density = LocalDensity.current
        val imageWidthDp = with(density) { imageWidthPx.toDp() }

        AppAsyncImage(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .onGloballyPositioned { coordinates ->
                    imageWidthPx = coordinates.size.width
                }
                .aspectRatio(16f / 9f),
            data = item.imageUrl,
            shadow = 5.dp,
            shape = AppTheme.shapes.roundSmall,
            contentDescription = "banner"
        )


        LabelMediumText(
            modifier = Modifier.width(imageWidthDp),
            textAlign = TextAlign.Start,
            text = item.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
