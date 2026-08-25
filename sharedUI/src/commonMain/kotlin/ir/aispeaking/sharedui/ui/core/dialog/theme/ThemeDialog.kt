package ir.aispeaking.sharedui.ui.core.dialog.theme


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.choose_the_application_theme
import ir.aispeaking.sharedui.ic_done
import ir.aispeaking.sharedui.ui.core.icon.AppIcon
import ir.aispeaking.sharedui.ui.core.space.VerticalSpace
import ir.aispeaking.sharedui.ui.core.text.BodyLargeBoldText
import ir.aispeaking.sharedui.ui.core.text.BodyLargeText
import ir.aispeaking.sharedui.ui.extension.animateClickable
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.them.AppTheme
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeDialog(
    vm: ThemeDialogViewModel = koinViewModel(),
    onDismiss: () -> Unit
) {
    // Removed LocalActivity.current - CMP does not have Activities
    val uiState = vm.uiState.collectAsState().value

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceContainerLow
    ) {
        ThemeDialogContent(
            modes = uiState.modes,
            onSelectMode = { theme ->
                // Fire and forget - let the reactive architecture handle the theme change
                vm.onTriggerEvent(ThemeDialogUiEvent.Save(theme))
            },
            current = uiState.current,
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ThemeDialogContent(
    modes: ImmutableList<ThemeMode>,
    onSelectMode: (ThemeMode) -> Unit,
    current: ThemeMode,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
            .padding(bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, alignment = Alignment.CenterVertically)
    ) {
        BodyLargeBoldText(text = stringResource(Res.string.choose_the_application_theme))

        VerticalSpace()

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(32.dp, alignment = Alignment.CenterVertically),
        ) {
            items(items = modes, key = { it.title.key }) // Ensure key is a String/Primitive, not a StringResource
            { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateClickable {
                            onSelectMode(theme)
                            onDismiss()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = Alignment.CenterHorizontally)
                ) {
                    AppIcon(
                        icon = theme.icon,
                        tint = AppTheme.colors.onSurface
                    )

                    BodyLargeText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = stringResource(theme.title),
                        color = if (current.key == theme.key) AppTheme.colors.primary else AppTheme.colors.onSurface
                    )

                    androidx.compose.animation.AnimatedVisibility(visible = current.key == theme.key) {
                        // Replaced standard material icon with the local resource
                        AppIcon(
                            icon = Res.drawable.ic_done,
                            tint = AppTheme.colors.primary
                        )
                    }
                }
            }
        }

        VerticalSpace()
    }
}