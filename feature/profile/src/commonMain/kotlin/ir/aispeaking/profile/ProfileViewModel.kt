package ir.aispeaking.profile

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.usecase.clear_shared.ClearSharedUseCase
import ir.aispeaking.domain.usecase.purchase.GetActivePurchasesUseCase
import ir.aispeaking.domain.usecase.user.GetServerUserUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.domain.usecase.user.UpdateUserUseCase
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.constant.AppConstant
import ir.aispeaking.utils.dLog
import ir.aispeaking.utils.immutableListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ProfileViewModel(
    private val getServerUserUseCase: GetServerUserUseCase,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
    private val getActivePurchasesUseCase: GetActivePurchasesUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val clearSharedUseCase: ClearSharedUseCase,
//    private val context: ContentType.Application,
) : BaseViewModel<ProfileUiState, ProfileUiEvent>() {

    init {
        getSharedPrefUser()
    }


    override fun createInitialState() = ProfileUiState()

    override fun onTriggerEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.FetchUser -> {
                if (AppConstant.needToFetchUser) {
                    getServerUser()
                } else {
                    getSharedPrefUser()
                }
                if (AppConstant.needToRefreshActivePurchase) getActivePurchase()
            }

            is ProfileUiEvent.GetActiveSubscriptions -> {
                getActivePurchase()
            }

            is ProfileUiEvent.OnPurchaseClick -> {
                setUiNavigation(ProfileUiNavigation.ToPurchase)
            }

            is ProfileUiEvent.ShowThemeDialog -> {
                setState { copy(showThemeDialog = event.show) }
            }

            is ProfileUiEvent.NewLanguageLevel -> {
                updateUser(event.languageLevel)
            }

            is ProfileUiEvent.NavigateToLogin -> {
                setUiNavigation(ProfileUiNavigation.ToLogin)
            }

            is ProfileUiEvent.NavigateToPurchaseHistory -> {
                setUiNavigation(ProfileUiNavigation.ToPurchaseHistory)
            }

            is ProfileUiEvent.NavigateToEditProfile -> {
                setUiNavigation(ProfileUiNavigation.ToEditProfile)
            }

            is ProfileUiEvent.OnExit -> {
                clearShared()
            }

            is ProfileUiEvent.ShowExitDialog -> {
                setState { copy(showExitDialog = event.show) }
            }

            is ProfileUiEvent.SetGuideRead -> {

            }

            is ProfileUiEvent.GuideTour -> {
                if (event.show)
                    setState { copy(guideList = GuideModel.getAll()) }
                else
                    setState { copy(guideList = immutableListOf()) }
            }

            is ProfileUiEvent.Support -> {
                openTelegramProfile()
            }
        }
    }

    private fun clearShared() {
        viewModelScope.launch {
            clearSharedUseCase.invoke()
            onTriggerEvent(ProfileUiEvent.NavigateToLogin)
        }
    }

    private fun updateUser(languageLevel: LanguageLevel) {
        setState { copy(updateUserLoading = true) }
        viewModelScope.launch {
            updateUserUseCase(currentState.user!!.copy(languageLevel = languageLevel)).collect {
                it.onSuccess {
                    setState { copy(updateUserLoading = false, user = currentState.user!!.copy(languageLevel = languageLevel)) }
                }.onFailure { apiError ->
                    setState { copy(updateUserLoading = false, subscriptionFetchingError = true) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun getActivePurchase() {
        setState { copy(isFetchingPurchases = true) }
        viewModelScope.launch {
            getActivePurchasesUseCase().collect {
                it.onSuccess { purchases ->
                    setState { copy(isFetchingPurchases = false, purchases = purchases.toImmutableList()) }
                }.onFailure { apiError ->
                    if (apiError is NetworkError.Unauthorized) {
                        setState { copy(isFetchingPurchases = false, user = null, subscriptionFetchingError = true) }
                    }
                    setState { copy(isFetchingPurchases = false, subscriptionFetchingError = true) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun getServerUser() {
        dLog("getServerUser")
        setState { copy(isFetchingUser = true) }
        viewModelScope.launch {
            getServerUserUseCase().collect {
                it.onSuccess { user ->
                    user.dLog("updated-user:")
                    setState { copy(user = user, isFetchingUser = false) }
                }.onFailure { apiError ->
                    apiError.dLog("updated-user:")
                    setState { copy(isFetchingUser = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

    private fun getSharedPrefUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect {
                it.onSuccess { user ->
                    if (currentState.user != user) {
                        setState { copy(isFetchingUser = false, user = user) }
                        getActivePurchase()
                    }
                }.onFailure { apiError ->
                    setState { copy(isFetchingUser = false) }
                }
            }
        }
    }

    private fun openTelegramProfile() {
        // TODO openTelegramProfile
//        val webUrl = "https://t.me/ai_speaking_support"
//        val intent = Intent(Intent.ACTION_VIEW, webUrl.toUri())
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        if (isAppInstalled()) {
//            intent.setPackage("org.telegram.messenger")
//        }
//
//        try {
//            context.startActivity(intent)
//        } catch (e: Exception) {
//            setUiMessage(UiMessage(intValue = Res.string.you_don_t_have_browser_please_in_telegram_search_ai_speaking_support))
//        }
    }

    private fun isAppInstalled(): Boolean {
        // TODO isAppInstalled
//        return try {
//            context.packageManager.getPackageInfo("org.telegram.messenger", PackageManager.GET_ACTIVITIES)
//            true
//        } catch (e: PackageManager.NameNotFoundException) {
//            false
//        }
        return false
    }

}
