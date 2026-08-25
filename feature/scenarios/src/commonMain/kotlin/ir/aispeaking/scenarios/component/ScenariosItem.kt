package ir.aispeaking.scenarios.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.scenario.ScenarioSummary
import ir.aispeaking.sharedui.ui.core.scenario.ScenarioSummaryItem
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ScenariosItem(
    modifier: Modifier,
    title: String,
    scenarios: ImmutableList<ScenarioSummary>,
    onScenarioClick: (ScenarioSummary) -> Unit,
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
                .requiredHeight(125.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start)
        ) {
            items(scenarios.size, key = { index -> scenarios[index].id })
            { index ->
                ScenarioSummaryItem(
                    modifier = Modifier
                        .animateItem(),
                    item = scenarios[index],
                    onScenarioClick = { onScenarioClick(scenarios[index]) }
                )
            }
        }
    }
}