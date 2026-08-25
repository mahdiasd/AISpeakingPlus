package ir.aispeaking.scenario_detail.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.scenario.Scenario
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_translate
import ir.aispeaking.sharedui.tasks_title
import ir.aispeaking.sharedui.tasks_title_fa
import ir.aispeaking.sharedui.ui.core.space.HorizontalSpace
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksContent(
    modifier: Modifier = Modifier,
    scenario: Scenario
) {
    var isEnglish by remember(scenario) { mutableStateOf(true) }
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp, alignment = Alignment.Top)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                modifier = Modifier
                    .size(28.dp)
                    .coloredShadow(AppTheme.colors.primary, alpha = 0.2f, shadowRadius = 20.dp)
                    .shadow(5.dp, shape = CircleShape)
                    .animateClickable {
                        isEnglish = !isEnglish
                    }
                    .background(AppTheme.colors.primary, CircleShape)
                    .padding(5.dp),
                painter = painterResource(Res.drawable.ic_translate),
                colorFilter = ColorFilter.tint(AppTheme.colors.onPrimary),
                contentDescription = "Translate",
            )
            AnimatedContent(isEnglish) {
                when (it) {
                    true -> {
                        BodyLargeBoldText(
                            text = stringResource(Res.string.tasks_title),
                            textAlign = TextAlign.Center,
                        )
                    }

                    false -> {
                        BodyLargeBoldText(
                            text = stringResource(Res.string.tasks_title_fa),
                            textAlign = TextAlign.Center,
                            textStyle = AppTheme.typography.bodyMediumBold.copy(fontFamily = AppTheme.typography.persianBold),
                        )
                    }
                }
            }

            HorizontalSpace()
        }


        VerticalSpace()

        scenario.tasks.forEachIndexed { index, scenarioTask ->
            key(scenarioTask.id) {

                AnimatedContent(isEnglish) {
                    when (it) {
                        true -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
                            ) {
                                BodyMediumBoldText(text = "${index + 1}.", color = AppTheme.colors.onSurface)
                                BodyMediumText(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    text = scenarioTask.description,
                                    textAlign = TextAlign.Start
                                )
                            }
                        }

                        false -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
                            ) {
                                BodyMediumText(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    text = scenarioTask.persianDescription,
                                    textAlign = TextAlign.Start,
                                    textDirection = TextDirection.Rtl,
                                    textStyle = AppTheme.typography.bodyMedium.copy(fontFamily = AppTheme.typography.persianRegular),
                                )
                                BodyMediumBoldText(
                                    text = "${index + 1}.",
                                    color = AppTheme.colors.onSurface,
                                    textDirection = TextDirection.Rtl,
                                    textStyle = AppTheme.typography.bodyMedium.copy(fontFamily = AppTheme.typography.persianBold),
                                )
                            }
                        }
                    }
                }

                VerticalSpace(16.dp)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        TasksContent(
            modifier = Modifier.fillMaxWidth(),
            FakeData.provideScenarios().first()
        )
    }
}