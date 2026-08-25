package ir.aispeaking.main

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.usecase.theme_mode.ReadThemeModeUseCase
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val readThemeModeUseCase: ReadThemeModeUseCase,
) : BaseViewModel<MainUiState, MainUiEvent>() {
    init {
        onTriggerEvent(MainUiEvent.CheckTheme)
    }

    override fun createInitialState() = MainUiState()

    override fun onTriggerEvent(event: MainUiEvent) {
        when (event) {
            is MainUiEvent.OnChangeTab -> {
                setUiNavigation(MainUiNavigation.ToPage(event.bottomBarItem))
                setState { copy(currentBottomBarItem = event.bottomBarItem) }
            }

            is MainUiEvent.ShowExitAppDialog -> {
                setState { copy(showExitDialog = event.show) }
            }

            is MainUiEvent.CheckTheme -> readThemMode()
        }
    }

    private fun readThemMode() {
        viewModelScope.launch {
            val mode = readThemeModeUseCase().mode
            if (currentState.themeMode.key != mode)
                setState { copy(themeMode = ThemeMode.mapper(mode)) }
        }
    }

}