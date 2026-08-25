package ir.aispeaking.sharedui.ui.core.dialog.theme

import androidx.compose.runtime.Stable
import ir.aispeaking.sharedui.ui.extension.immutableListOf
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList

@Stable
data class ThemeDialogUiState(
    val modes: ImmutableList<ThemeMode> = immutableListOf(ThemeMode.System(), ThemeMode.Light(), ThemeMode.Dark()),
    val current: ThemeMode = ThemeMode.System(),
) : UiState

sealed class ThemeDialogUiEvent : UiEvent {
    data object CheckTheme : ThemeDialogUiEvent()
    data class Save(val themeMode: ThemeMode) : ThemeDialogUiEvent()
}
