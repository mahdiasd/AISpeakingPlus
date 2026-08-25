package ir.aispeaking.scenario_detail.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.level.LanguageGroupLevel
import ir.aispeaking.domain.model.level.copy
import ir.aispeaking.scenario_detail.OnAction
import ir.aispeaking.scenario_detail.ScenarioDetailUiEvent
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_back
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.LabelMediumBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.PointsColor
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Composable
internal fun TopContent(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    onAction: OnAction,
    aiAvatar: String?,
    levelGroups: ImmutableList<LanguageGroupLevel>?,
    points: String = "0",
    onBack: () -> Unit = {}
) {
    ConstraintLayout(
        modifier = modifier,
    ) {
        val (imageRef) = createRefs()

        AppAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(imageRef) {
                    top.linkTo(parent.top)
                }
                .aspectRatio(16f / 9f),
            placeholderColor = AppTheme.colors.outline.copy(alpha = 0.2f),
            data = imageUrl,
        )

        AppIcon(
            modifier = Modifier
                .constrainAs(createRef())
                {
                    top.linkTo(parent.top, 8.dp)
                    start.linkTo(parent.start, 16.dp)
                }
                .border(0.5.dp, AppTheme.colors.surfaceContainerHighest, shape = CircleShape)
                .animateClickable(onBack)
                .shadow(2.dp, shape = AppTheme.shapes.roundedSemiLarge)
                .background(AppTheme.colors.surface, CircleShape)
                .padding(8.dp),
            icon = Res.drawable.ic_back,
            size = 32.dp,
            tint = AppTheme.colors.onPrimaryContainer
        )

        DualContentRow(
            modifier = Modifier
                .constrainAs(createRef())
                {
                    top.linkTo(parent.top, 8.dp)
                    end.linkTo(parent.end, 16.dp)
                }
                .shadow(2.dp, shape = AppTheme.shapes.roundedSemiLarge)
                .background(AppTheme.colors.surface, shape = AppTheme.shapes.roundedSemiLarge)
                .border(0.5.dp, AppTheme.colors.surfaceContainerHighest, shape = AppTheme.shapes.roundedSemiLarge)
                .padding(vertical = 4.dp, horizontal = 8.dp),
            leftContent = {
                LabelMediumBoldText(
                    text = points,
                    color = AppTheme.colors.onPrimaryContainer
                )
            },
            rightContent = {
                AppIcon(
                    size = 14.dp,
                    icon = Res.drawable.ic_points,
                    tint = PointsColor
                )
            },
        )


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .constrainAs(createRef())
                {
                    top.linkTo(imageRef.bottom, 16.dp)
                    bottom.linkTo(imageRef.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AppAsyncImage(
                modifier = Modifier
                    .size(105.dp)
                    .border(
                        width = 3.dp,
                        color = AppTheme.colors.primary,
                        shape = CircleShape
                    )
                    .background(AppTheme.colors.surfaceContainer, shape = CircleShape),
                shape = CircleShape,
                data = aiAvatar
            )
            AnimatedContent(levelGroups != null) {
                when (it) {
                    true -> {
                        LanguageLevelGroupContent(
                            modifier = Modifier,
                            levelGroups = levelGroups!!,
                            onClick = { onAction(ScenarioDetailUiEvent.OnLanguageGroupLevel(it)) }
                        )
                    }

                    false -> {
                        Spacer(modifier = Modifier.width(50.dp))
                    }
                }
            }
        }


    }
}

@LightDarkPreview
@Composable
private fun TopContentPreview() {
    AppTheme {
        val fakeScenario = FakeData.provideScenarios().first()
        TopContent(
            modifier = Modifier.fillMaxWidth(),
            imageUrl = fakeScenario.imageUrl,
            aiAvatar = fakeScenario.aiAvatar,
            levelGroups = LanguageGroupLevel.entries.map { it.copy(selected = true) }.toImmutableList(),
            onAction = {},
        )
    }
}

@LightDarkPreview
@Composable
private fun TopContentPreviewWithNullValues() {
    AppTheme {
        TopContent(
            modifier = Modifier.fillMaxWidth(),
            imageUrl = null,
            aiAvatar = null,
            levelGroups = LanguageGroupLevel.entries,
            onAction = {},
        )
    }
}
