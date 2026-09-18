package ir.aispeaking.main

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.usecase.theme_mode.ReadThemeModeUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.sharedui.ui.model.theme_mode.ThemeMode
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class MainViewModel(
    private val readThemeModeUseCase: ReadThemeModeUseCase,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
) : BaseViewModel<MainUiState, MainUiEvent>() {
    init {
        onTriggerEvent(MainUiEvent.CheckTheme)
        onTriggerEvent(MainUiEvent.FetchUser)
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

            is MainUiEvent.FetchUser -> fetchUser()
        }
    }

    private fun readThemMode() {
        viewModelScope.launch {
            val mode = readThemeModeUseCase().mode
            if (currentState.themeMode.key != mode)
                setState { copy(themeMode = ThemeMode.mapper(mode)) }
        }
    }

    private fun fetchUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect { result ->
                result.onSuccess { user ->
                    val userScore = user.score
                    val calculatedLevel = ((userScore / 250) + 1).coerceAtLeast(1)
                    val calculatedCurrentXp = userScore % 1000
                    val greetingName = user.nickName.ifBlank { user.firstName.ifBlank { "قهرمان" } }
                    val speech = "سلام $greetingName! آماده‌ای برای ماجراجویی مرحله $calculatedLevel؟"
                    setState {
                        copy(
                            user = user,
                            level = calculatedLevel,
                            currentXp = if (calculatedCurrentXp == 0 && userScore > 0) 750 else calculatedCurrentXp,
                            speechText = speech,
                            activeLevelTitle = "مرحله $calculatedLevel",
                        )
                    }
                }
            }
        }
    }
}