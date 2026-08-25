package ir.aispeaking.onboarding

import androidx.compose.runtime.Stable
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

@Stable
data class OnboardingUiState(
    val onboardingPages: ImmutableList<OnboardingPage> = OnboardingPage.pages.toImmutableList(),
    val selectedPage: OnboardingPage = OnboardingPage.First
) : UiState

sealed class OnboardingUiEvent : UiEvent {
    data object OnNavigateToMain : OnboardingUiEvent()
    data class OnChangePage(val page: OnboardingPage) : OnboardingUiEvent()
}

sealed class OnboardingUiNavigation : UiNavigation {
    data object ToMain : OnboardingUiNavigation()
}

typealias OnAction = (OnboardingUiEvent) -> Unit


