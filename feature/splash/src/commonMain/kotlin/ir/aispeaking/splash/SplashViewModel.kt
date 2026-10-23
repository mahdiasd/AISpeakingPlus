package ir.aispeaking.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.usecase.auth.AuthStatus
import ir.aispeaking.domain.usecase.auth.CheckAuthStatusUseCase
import ir.aispeaking.domain.usecase.auth.HasSeenIntroVideoUseCase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class SplashViewModel(
    private val checkAuthStatusUseCase: CheckAuthStatusUseCase,
    private val hasSeenIntroVideoUseCase: HasSeenIntroVideoUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect = _effect.receiveAsFlow()

    init {
        processIntent(SplashIntent.CheckAuth)
    }

    fun processIntent(intent: SplashIntent) {
        when (intent) {
            is SplashIntent.CheckAuth -> checkAuth()
            is SplashIntent.Retry -> checkAuth()
        }
    }

    private fun checkAuth() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Splash delay to allow user to see branding
            delay(1200)

            try {
                val hasSeenIntro = hasSeenIntroVideoUseCase()
                when (val status = checkAuthStatusUseCase()) {
                    is AuthStatus.Authenticated -> {
                        _uiState.update { it.copy(isLoading = false) }
                        if (!hasSeenIntro) {
                            _effect.send(SplashEffect.NavigateToIntroVideo)
                        } else {
                            _effect.send(SplashEffect.NavigateToMain(status.user))
                        }
                    }
                    is AuthStatus.Unauthenticated -> {
                        _uiState.update { it.copy(isLoading = false) }
                        if (!hasSeenIntro) {
                            _effect.send(SplashEffect.NavigateToIntroVideo)
                        } else {
                            _effect.send(SplashEffect.NavigateToMain(null))
                        }
                    }
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "خطا در برقراری ارتباط با سرور"
                    )
                }
            }
        }
    }
}
