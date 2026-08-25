package ir.aispeaking.sharedui.ui.core.dialog.language_level

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.sharedui.ui.core.text.BodyMediumText
import ir.aispeaking.sharedui.ui.extension.LightDarkPreview
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.extension.name
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelDialog(
    onNewLevel: (LanguageLevel) -> Unit,
    currentLevel: LanguageLevel,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainerLow
    ) {
        LevelDialogContent(
            onNewLevel = onNewLevel,
            currentLevel = currentLevel,
        )
    }
}

@Composable
private fun LevelDialogContent(
    onNewLevel: (LanguageLevel) -> Unit,
    currentLevel: LanguageLevel,
) {
    val levels by remember { mutableStateOf(LanguageLevel.entries.toImmutableList()) }
    var selectedLevel: LanguageLevel? by remember { mutableStateOf(null) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp, alignment = Alignment.CenterVertically)
    ) {
        items(levels, key = { it.name })
        { languageLevel ->
            BodyMediumText(
                modifier = Modifier.animateClickable {
                    selectedLevel = languageLevel
                    onNewLevel(languageLevel)
                },
                text = stringResource(languageLevel.name()),
                color = if (currentLevel == languageLevel) AppTheme.colors.primary else AppTheme.colors.onSurface
            )
        }
    }

}

@LightDarkPreview
@Composable
private fun Preview() {
    AppTheme {
        LevelDialogContent(onNewLevel = { }, currentLevel = LanguageLevel.A1)
    }
}