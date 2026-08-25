package ir.aispeaking.scenario_detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.level.LanguageGroupLevel
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.ic_arrow_left
import ir.aispeaking.sharedui.level
import ir.aispeaking.sharedui.level_group_dialog_title
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.text.BodyLargeText
import ir.aispeaking.sharedui.ui.core.text.LabelMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.name
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageLevelGroupContent(
    modifier: Modifier = Modifier,
    levelGroups: ImmutableList<LanguageGroupLevel>,
    onClick: (LanguageGroupLevel) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val selectedGroup by remember(levelGroups) { derivedStateOf { levelGroups.find { it.selected } } }

    selectedGroup?.let { selected ->
        Row(
            modifier = modifier
                .padding(horizontal = 8.dp)
                .shadow(1.dp, shape = AppTheme.shapes.roundedSemiLarge)
                .background(
                    color = AppTheme.colors.primary,
                    shape = AppTheme.shapes.roundSmall
                )
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .animateClickable {
                    showDialog = true
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            AppIcon(
                modifier = Modifier.rotate(-90f),
                icon = Res.drawable.ic_arrow_left,
                size = 8.dp,
                tint = AppTheme.colors.onPrimary
            )

            LabelMediumText(
                text = "${stringResource(selected.name())} ${stringResource(Res.string.level)}",
                textAlign = TextAlign.Center,
                color = AppTheme.colors.onPrimary
            )

        }
    }


    if (showDialog) {
        ModalBottomSheet(
            onDismissRequest = { showDialog = false },
            containerColor = AppTheme.colors.surfaceContainerHighest
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
            ) {
                BodyLargeText(
                    text = stringResource(Res.string.level_group_dialog_title),
                    textAlign = TextAlign.Center
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .padding(vertical = 16.dp),
                    color = AppTheme.colors.outline
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp, alignment = Alignment.CenterVertically)

                ) {
                    items(levelGroups.count(), key = { levelGroups[it].name() })
                    {
                        BodyLargeText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .alpha(if (levelGroups[it].haveAccess) 1f else 0.5f)
                                .animateClickable {
                                    onClick(levelGroups[it])
                                    showDialog = false
                                },
                            text = stringResource(levelGroups[it].name()),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        val languageGroupLevel = LanguageGroupLevel.Basic()
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            LanguageLevelGroupContent(
                modifier = Modifier,
                levelGroups = LanguageGroupLevel.entries,
                onClick = { }
            )
        }
    }
}