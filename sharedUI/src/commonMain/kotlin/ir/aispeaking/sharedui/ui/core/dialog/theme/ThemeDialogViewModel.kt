package ir.aispeaking.sharedui.ui.core.dialog.theme

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.usecase.theme_mode.ReadThemeModeUseCase
import ir.aispeaking.domain.usecase.theme_mode.SaveThemeModeUseCase
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode

import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ThemeDialogViewModel(
    private val readThemeModeUseCase: ReadThemeModeUseCase,
    private val saveThemModeUseCase: SaveThemeModeUseCase,
) : BaseViewModel<ThemeDialogUiState, ThemeDialogUiEvent>() {

    init {
        onTriggerEvent(ThemeDialogUiEvent.CheckTheme)
    }

    override fun createInitialState() = ThemeDialogUiState()

    override fun onTriggerEvent(event: ThemeDialogUiEvent) {
        when (event) {
            ThemeDialogUiEvent.CheckTheme -> readThemMode()
            is ThemeDialogUiEvent.Save -> {
                saveThemMode(event.themeMode)
            }
        }
    }

    private fun readThemMode() {
        viewModelScope.launch {
            val mode = readThemeModeUseCase().mode
            setState { copy(current = ThemeMode.mapper(mode)) }
        }
    }

    private fun saveThemMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            // Save the newly selected theme to persistent storage
            saveThemModeUseCase(themeMode.key)

            // Instantly update the dialog's local UI state
            setState { copy(current = themeMode) }
        }
    }
}