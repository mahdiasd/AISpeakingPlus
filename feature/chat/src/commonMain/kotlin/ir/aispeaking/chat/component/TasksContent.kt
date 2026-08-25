package ir.aispeaking.chat.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.scenario.ScenarioTask
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.ic_checked
import ir.aispeaking.sharedui.ic_translate
import ir.aispeaking.sharedui.ic_unchecked
import ir.aispeaking.sharedui.tasks_dialog_title
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksContent(
    modifier: Modifier,
    tasks: ImmutableList<ScenarioTask>,
    onBackClick: () -> Unit,
) {
    var isEnglish by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, AppTheme.colors.outlineVariant, shape = AppTheme.shapes.roundMedium)
            .background(AppTheme.colors.surfaceContainerLow.copy(alpha = 0.9f), shape = AppTheme.shapes.roundMedium)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterVertically)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
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
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(AppTheme.colors.onPrimary),
                contentDescription = "Translate",
            )

            BodyMediumBoldText(text = stringResource(Res.string.tasks_dialog_title))

            AppIcon(
                modifier = Modifier
                    .rotate(90f)
                    .padding(4.dp),
                size = 24.dp,
                icon = Res.drawable.ic_arrow_left,
                onClick = { onBackClick() },
                tint = AppTheme.colors.onSurface
            )
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth())
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            items(items = tasks, key = { task -> task.id })
            { task ->
                DualContentRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterHorizontally),
                    leftContent = {
                        AnimatedContent(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            targetState = isEnglish
                        ) {
                            when (it) {
                                true -> {
                                    BodyMediumText(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        persianFont = false,
                                        text = task.description
                                    )
                                }

                                false -> {
                                    BodyMediumText(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        textAlign = TextAlign.Start,
                                        persianFont = true,
                                        text = task.persianDescription
                                    )
                                }
                            }
                        }
                    },
                    rightContent = {
                        AnimatedContent(task.finished) { isFinished ->
                            when (isFinished) {
                                true -> {
                                    AppIcon(
                                        icon = Res.drawable.ic_checked,
                                        tint = AppTheme.colors.success,
                                    )
                                }

                                false -> {
                                    AppIcon(
                                        icon = Res.drawable.ic_unchecked,
                                        tint = AppTheme.colors.outline
                                    )
                                }
                            }
                        }

                    }
                )
            }
        }
    }


}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth())
        {
            TasksContent(
                modifier = Modifier
                    .fillMaxWidth(),
                FakeData.provideTasks(),
                onBackClick = {}
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun FinishedPreview() {
    AppTheme {
        Box(modifier = Modifier.fillMaxWidth())
        {
            TasksContent(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.8f),
                onBackClick = {},
                tasks = FakeData.provideTasks().map { it.copy(finished = true) }.toImmutableList()
            )
        }
    }
}
