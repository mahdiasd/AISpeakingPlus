package ir.aispeaking.onboarding

import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiState
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.domain.usecase.onboarding.SetOnboardingUseCase
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class OnboardingViewModel(
    private val setOnboardingUseCase: SetOnboardingUseCase
) : BaseViewModel<OnboardingUiState, OnboardingUiEvent>() {

    init {
        setOnBoarding()
    }

    private fun setOnBoarding() {
        viewModelScope.launch {
            setOnboardingUseCase.invoke()
        }
    }

    override fun createInitialState() = OnboardingUiState()

    override fun onTriggerEvent(event: OnboardingUiEvent) {
        when (event) {
            is OnboardingUiEvent.OnChangePage -> {
                setState { copy(selectedPage = event.page) }
            }

            is OnboardingUiEvent.OnNavigateToMain -> {
                setUiNavigation(OnboardingUiNavigation.ToMain)
            }
        }
    }

}


