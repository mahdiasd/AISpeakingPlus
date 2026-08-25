package ir.aispeaking.competition.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import ir.aispeaking.competition.CompetitionUiEvent
import ir.aispeaking.competition.OnAction
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.challenge.ChallengeSummary
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_points
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.image.AppAsyncImage
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.core.text.DualContentRow
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.coloredShadow
import ir.aispeaking.sharedui.ui.them.AppTheme
import ir.aispeaking.sharedui.ui.them.PointsColor

@Composable
fun ChallengeSection(
    modifier: Modifier,
    challengeSummary: ChallengeSummary,
    onAction: OnAction
) {
    ConstraintLayout(modifier = modifier) {
        val boxRef = createRef()
        Column(
            modifier = Modifier
                .constrainAs(boxRef) {
                    top.linkTo(parent.top)
                }
                .fillMaxWidth()
                .animateClickable { onAction(CompetitionUiEvent.OnNavigateToChallenge) }
                .shadow(2.dp, shape = AppTheme.shapes.roundMedium)
                .coloredShadow(
                    color = AppTheme.colors.primary,
                    borderRadius = 10.dp,
                    shadowRadius = 10.dp,
                    offsetY = 2.dp,
                    alpha = 0.6f
                )
                .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
                .padding(8.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            DualContentRow(
                rightContent = {
                    BodyMediumBoldText(text = "${challengeSummary.score}")
                },
                leftContent = {
                    AppIcon(
                        icon = Res.drawable.ic_points,
                        tint = PointsColor,
                        size = 16.dp
                    )
                },
            )
            TitleBoldText(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = challengeSummary.title,
                color = AppTheme.colors.primary
            )
            BodyMediumText(
                modifier = Modifier.fillMaxWidth(),
                text = challengeSummary.description,
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onSurface
            )
        }

        AppAsyncImage(
            modifier = Modifier
                .background(AppTheme.colors.outline , shape = CircleShape)
                .constrainAs(createRef()) {
                    top.linkTo(boxRef.top)
                    bottom.linkTo(boxRef.top)
                    end.linkTo(boxRef.end)
                }
                .size(80.dp),
            data = challengeSummary.aiAvatar,
            shape = CircleShape,
            placeholderColor = AppTheme.colors.outline
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        ChallengeSection(
            modifier = Modifier.fillMaxWidth(),
            challengeSummary = FakeData.provideChallengeSummary(),
            onAction = {}
        )
    }
}
