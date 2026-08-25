package ir.aispeaking.splash

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.config.Update
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState

@Stable
data class SplashUiState(
    val splashState: SplashState = SplashState.FetchingData,
) : UiState


sealed class SplashUiEvent : UiEvent {
    data object OnRefreshClick : SplashUiEvent()
    data object MoveToMain : SplashUiEvent()
    data object UpdateApp : SplashUiEvent()
}

typealias OnAction = (SplashUiEvent) -> Unit

sealed class SplashState {
    data object FetchingData : SplashState()

    data object FetchingError : SplashState()

    data class DataFetched(val update: Update) : SplashState()
}

sealed class SplashUiNavigation : UiNavigation {
    data object ToMain : SplashUiNavigation()
    data object ToOnboarding : SplashUiNavigation()
    data object ToAuth : SplashUiNavigation()
}
