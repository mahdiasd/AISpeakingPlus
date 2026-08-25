package ir.aispeaking.profile.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.fake_data.FakeData
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.profile.OnAction
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.competitions
import ir.aispeaking.sharedui.complete_scenarios
import ir.aispeaking.sharedui.complete_words
import ir.aispeaking.sharedui.ic_challenge
import ir.aispeaking.sharedui.ic_scenario
import ir.aispeaking.sharedui.ic_word
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.core.text.TitleBoldText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.iconSize
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DataSection(
    modifier: Modifier,
    user: User,
    onAction: OnAction
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        DataSectionItem(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 100.dp),
            icon = Res.drawable.ic_scenario,
            text = Res.string.complete_scenarios,
            number = user.completedScenarioCount
        )

        DataSectionItem(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 100.dp),
            icon = Res.drawable.ic_word,
            text = Res.string.complete_words,
            number = user.completedWordCount
        )

        DataSectionItem(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .widthIn(max = 100.dp),
            icon = Res.drawable.ic_challenge,
            text = Res.string.competitions,
            number = user.completedChallengeCount
        )
    }
}

@Composable
private fun DataSectionItem(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    text: StringResource,
    number: Long
) {
    Column(
        modifier = modifier
            .background(AppTheme.colors.surfaceContainerLow, shape = AppTheme.shapes.roundMedium)
            .border(width = 1.dp, color = AppTheme.colors.outline, shape = AppTheme.shapes.roundMedium)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, alignment = Alignment.CenterVertically)
    ) {
        Image(
            modifier = Modifier
                .iconSize(52.dp)
                .background(AppTheme.colors.primaryContainer, shape = CircleShape)
                .border(0.5.dp, color = AppTheme.colors.onPrimaryContainer, shape = CircleShape)
                .padding(12.dp),
            contentDescription = "",
            colorFilter = ColorFilter.tint(AppTheme.colors.onPrimaryContainer),
            painter = painterResource(icon)
        )

        LabelMediumText(
            minLines = 2,
            text = stringResource(text),
            textAlign = TextAlign.Center
        )

        TitleBoldText(
            text = number.toString(),
            color = AppTheme.colors.onPrimaryContainer
        )
    }
}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        DataSection(
            modifier = Modifier.wrapContentWidth(),
            user = FakeData.provideUsers().first(),
            onAction = {}
        )
    }
}
