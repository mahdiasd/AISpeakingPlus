package ir.aispeaking.profile

import androidx.compose.runtime.Stable
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.purchase.Purchase
import ir.aispeaking.domain.model.user.User
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.viewmodel.UiEvent
import ir.aispeaking.sharedui.viewmodel.UiNavigation
import ir.aispeaking.sharedui.viewmodel.UiState
import ir.aispeaking.utils.immutableListOf
import kotlinx.collections.immutable.ImmutableList

@Stable
data class ProfileUiState(
    val isFetchingUser: Boolean = true,
    val isFetchingPurchases: Boolean = false,
    val subscriptionFetchingError: Boolean = false,
    val user: User? = null,
    val purchases: ImmutableList<Purchase>? = null,

    val showThemeDialog: Boolean = false,
    val showExitDialog: Boolean = false,

    val updateUserLoading: Boolean = false,

    val guideList: ImmutableList<GuideModel> = immutableListOf(),

    ) : UiState


sealed class ProfileUiEvent : UiEvent {
    data object SetGuideRead : ProfileUiEvent()

    data object FetchUser : ProfileUiEvent()
    data object OnExit : ProfileUiEvent()
    data object GetActiveSubscriptions : ProfileUiEvent()
    data object OnPurchaseClick : ProfileUiEvent()
    data object NavigateToLogin : ProfileUiEvent()
    data object NavigateToPurchaseHistory : ProfileUiEvent()
    data object NavigateToEditProfile : ProfileUiEvent()
    data object Support : ProfileUiEvent()
    data class ShowThemeDialog(val show: Boolean) : ProfileUiEvent()
    data class GuideTour(val show: Boolean) : ProfileUiEvent()
    data class ShowExitDialog(val show: Boolean) : ProfileUiEvent()
    data class NewLanguageLevel(val languageLevel: LanguageLevel) : ProfileUiEvent()
}

sealed class ProfileUiNavigation : UiNavigation {
    data object ToBack : ProfileUiNavigation()
    data object ToPurchase : ProfileUiNavigation()
    data object ToLogin : ProfileUiNavigation()
    data object ToPurchaseHistory : ProfileUiNavigation()
    data object ToEditProfile : ProfileUiNavigation()
}

typealias OnAction = (ProfileUiEvent) -> Unit
