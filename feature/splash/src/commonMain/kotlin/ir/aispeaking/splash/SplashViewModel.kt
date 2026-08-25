package ir.aispeaking.splash

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.config.ConfigRequest
import ir.aispeaking.domain.model.config.UpdateState
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.usecase.config.GetConfigUseCase
import ir.aispeaking.domain.usecase.onboarding.OnboardingUseCase
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.no_browser_installed
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.ui_message.MessageStatus
import ir.aispeaking.sharedui.ui.model.ui_message.MessageType
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.dLog
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel


@KoinViewModel
class SplashViewModel(
    private val getConfigUseCase: GetConfigUseCase,
    private val onboardingUseCase: OnboardingUseCase,
) : BaseViewModel<SplashUiState, SplashUiEvent>() {
    private var onboardingIsSaw: Boolean = false
    private var firebaseToken = ""

    init {
        subscribeToPublicTopic()
        getOnboardingIsSaw()
        fetchFirebaseToken()
    }
    private fun fetchFirebaseToken() {
        // Platform specific - placeholder
        firebaseToken = ""
        getConfig()
    }

    private fun getOnboardingIsSaw() {
        viewModelScope.launch {
            onboardingIsSaw = onboardingUseCase.invoke()
        }
    }

    override fun createInitialState() = SplashUiState()

    override fun onTriggerEvent(event: SplashUiEvent) {
        when (event) {
            SplashUiEvent.OnRefreshClick -> getConfig()
            SplashUiEvent.MoveToMain -> setUiNavigation(SplashUiNavigation.ToMain)
            SplashUiEvent.UpdateApp -> {
                openCafeBazaar()
            }
        }
    }

    fun openCafeBazaar() {
        // Platform specific - placeholder
        setUiMessage(
            UiMessage(
                intValue = Res.string.no_browser_installed,
                messageType = MessageType.Device,
                status = MessageStatus.Failure
            )
        )
    }

    private fun getConfig() {
        val deviceName = "Multiplatform Device"
        val androidVersion = "8"

        setState { copy(splashState = SplashState.FetchingData) }

        viewModelScope.launch {
            getConfigUseCase(
                ConfigRequest(
                    deviceName = deviceName,
                    androidVersion = androidVersion,
                    firebaseToken = firebaseToken
                )
            ).collect {
                it.onSuccess { config ->
                    setState { copy(splashState = SplashState.DataFetched(config.update)) }
                    // TODO: Pass actual version code for KMP
                    if (config.update.getState(8) is UpdateState.UpToDate) {
                        if (onboardingIsSaw)
                            setUiNavigation(SplashUiNavigation.ToMain)
                        else
                            setUiNavigation(SplashUiNavigation.ToOnboarding)
                    }
                }.onFailure { appError ->
                    if (appError is NetworkError.Unauthorized) {
                        setUiNavigation(SplashUiNavigation.ToAuth)
                    } else {
                        setState { copy(splashState = SplashState.FetchingError) }
                        setUiMessage(appError.toUiMessage())
                    }
                }
            }
        }
    }

    fun subscribeToPublicTopic() {
        "subscribe to channel".dLog()
    }
}
