package ir.aispeaking.register.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.sharedui.ui.core.text.BodyMediumBoldText
import ir.aispeaking.sharedui.ui.core.text.LabelSmallText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.descriptionToPersian
import ir.aispeaking.sharedui.ui.extension.name
import ir.aispeaking.sharedui.ui.them.AppTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LevelItem(
    languageLevel: LanguageLevel,
    onClick: () -> Unit,
    isSelected: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = AppTheme.shapes.roundMedium,
                spotColor = AppTheme.colors.outline
            )
            .background(
                color = when {
                    isSelected -> AppTheme.colors.primaryContainer
                    else -> AppTheme.colors.surfaceContainerLowest
                },
                shape = AppTheme.shapes.roundMedium
            )
//            .border(1.dp, AppTheme.colors.outlineVariant, shape = AppTheme.shapes.roundMedium)
            .padding(16.dp)
            .animateClickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        BodyMediumBoldText(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = when {
                isSelected -> AppTheme.colors.onPrimaryContainer
                else -> AppTheme.colors.onSurface
            },
            text = stringResource(languageLevel.name())
        )
        LabelSmallText(text = stringResource(languageLevel.descriptionToPersian()))
    }
}

@Preview
@Composable
private fun Preview() {
    AppTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
        ) {
            LevelItem(LanguageLevel.B1, onClick = {}, false)
            LevelItem(LanguageLevel.B1, onClick = {}, true)
        }
    }
}