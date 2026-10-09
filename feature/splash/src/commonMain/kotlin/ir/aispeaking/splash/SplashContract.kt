package ir.aispeaking.splash

import ir.aispeaking.domain.model.user.User

data class SplashUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

sealed interface SplashIntent {
    data object CheckAuth : SplashIntent
    data object Retry : SplashIntent
}

sealed interface SplashEffect {
    data class NavigateToMain(val user: User) : SplashEffect
    data object NavigateToLogin : SplashEffect
    data class ShowToast(val message: String) : SplashEffect
}
