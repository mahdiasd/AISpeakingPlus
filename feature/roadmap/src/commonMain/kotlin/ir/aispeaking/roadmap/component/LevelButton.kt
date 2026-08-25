package ir.aispeaking.roadmap.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.aispeaking.roadmap.model.LevelData
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_roadmap_level_1_not_passed
import ir.aispeaking.sharedui.ic_roadmap_level_1_passed
import ir.aispeaking.sharedui.ic_roadmap_level_2_not_passed
import ir.aispeaking.sharedui.ic_roadmap_level_2_passed
import ir.aispeaking.sharedui.roadmap_background
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import kotlin.random.Random

@Composable
fun LevelButton(
    modifier: Modifier,
    item: LevelData,
    shapeSize: Dp = 100.dp
) {
    val shape = remember {
        mutableStateListOf(
            Res.drawable.ic_roadmap_level_1_passed,
            Res.drawable.ic_roadmap_level_2_passed,

            Res.drawable.ic_roadmap_level_1_not_passed,
            Res.drawable.ic_roadmap_level_2_not_passed,
        )
    }

    val levelShape by remember(item) {
        derivedStateOf {
            when (item.isPassed) {
                true -> shape[Random.nextInt(0, 2)]
                false -> shape[Random.nextInt(2, 4)]
            }
        }
    }

    Box(modifier = modifier.size(shapeSize), contentAlignment = Alignment.Center)
    {
        Image(
            modifier = Modifier
                .matchParentSize()
                .align(alignment = Alignment.BottomStart),
            painter = painterResource(levelShape),
            contentDescription = "",
        )

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            LabelMediumBoldText(text = item.title, color = AppTheme.colors.onPrimary)
            LabelMediumBoldText(text = item.rangeText, color = AppTheme.colors.onPrimary)
        }
    }
}


@Preview
@Composable
private fun Preview() {
    AppTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .paint(painterResource(Res.drawable.roadmap_background), contentScale = ContentScale.FillBounds),
            horizontalArrangement = Arrangement.spacedBy(24.dp, alignment = Alignment.CenterHorizontally),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {

                BodyMediumBoldText(text = "Not Passed: ", color = AppTheme.colors.onPrimary)
                LevelButton(
                    modifier = Modifier.size(120.dp),
                    item = LevelData(
                        level = 5,
                        title = "Level 2",
                        rangeText = "1-50",
                        isPassed = false
                    )
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {
                BodyMediumBoldText(text = "Passed: ", color = AppTheme.colors.onPrimary)
                LevelButton(
                    modifier = Modifier.size(120.dp),
                    item = LevelData(
                        level = 5,
                        title = "Level 2",
                        rangeText = "1-50",
                        isPassed = true
                    )
                )
            }
        }
    }
}