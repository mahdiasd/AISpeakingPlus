package ir.aispeaking.scenarios

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.config.WelcomeMessage
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.usecase.guide.ReadGuideStatusUseCase
import ir.aispeaking.domain.usecase.guide.SetGuideForChallenge
import ir.aispeaking.domain.usecase.guide.SetGuideForLightener
import ir.aispeaking.domain.usecase.guide.SetGuideForProfile
import ir.aispeaking.domain.usecase.guide.SetGuideForRoadmap
import ir.aispeaking.domain.usecase.guide.SetGuideForScenarios
import ir.aispeaking.domain.usecase.home.GetHomeUseCase
import ir.aispeaking.domain.usecase.welcome_message.ReadWelcomeMessageUseCase
import ir.aispeaking.domain.usecase.welcome_message.SaveWelcomeMessageUseCase
import ir.aispeaking.scenarios.ScenariosUiNavigation.ToScenarioDetail
import ir.aispeaking.scenarios.ScenariosUiNavigation.ToSearch
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.guide.GuideModel
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.dLog
import ir.aispeaking.utils.immutableListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScenariosViewModel(
    private val getHomeUseCase: GetHomeUseCase,
    private val readGuideStatusUseCase: ReadGuideStatusUseCase,

    private val setGuideForProfile: SetGuideForProfile,
    private val setGuideForChallenge: SetGuideForChallenge,
    private val setGuideForLightener: SetGuideForLightener,
    private val setGuideForRoadmap: SetGuideForRoadmap,
    private val setGuideForScenarios: SetGuideForScenarios,

    private val saveWelcomeMessageUseCase: SaveWelcomeMessageUseCase,
    private val readWelcomeMessageUseCase: ReadWelcomeMessageUseCase
) : BaseViewModel<ScenariosUiState, ScenariosUiEvent>() {

    init {
        dLog("ScenariosViewModel")
        readGuideStatus()

        readWelcomeMessage()

        onTriggerEvent(ScenariosUiEvent.OnRefreshList)
    }

    fun readGuideStatus() {
        viewModelScope.launch {
            if (!readGuideStatusUseCase.invoke().scenario || !readGuideStatusUseCase.invoke().profile) {
                setState {
                    copy(
                        guideList = immutableListOf(
                            GuideModel.ScenarioGuideModel(),
                            GuideModel.ChallengeGuideModel(),
                            GuideModel.ProfileGuideModel(),
                            GuideModel.LightenerGuideModel(),
                            GuideModel.RoadmapGuideModel(),

                            )
                    )
                }
            }
        }
    }

    fun readWelcomeMessage() {
        viewModelScope.launch {
            readWelcomeMessageUseCase()?.let {
                if (!it.isReadByUser)
                    setState { copy(welcomeMessage = it) }
            }
        }
    }

    fun saveWelcomeMessage(welcomeMessage: WelcomeMessage?) {
        viewModelScope.launch {
            saveWelcomeMessageUseCase(welcomeMessage)
        }
    }

    fun setReadGuide() {
        viewModelScope.launch {
            setState { copy(guideList = immutableListOf()) }
            setGuideForProfile.invoke()
            setGuideForChallenge.invoke()
            setGuideForLightener.invoke()
            setGuideForRoadmap.invoke()
            setGuideForScenarios.invoke()
        }
    }

    override fun createInitialState() = ScenariosUiState()

    override fun onTriggerEvent(event: ScenariosUiEvent) {
        when (event) {
            is ScenariosUiEvent.OnCategoryClick -> {
                setUiNavigation(ToSearch(event.category))
            }

            is ScenariosUiEvent.OnRefreshList -> {
                getHome()
            }

            is ScenariosUiEvent.OnBannerClick -> {

            }

            is ScenariosUiEvent.OnScenarioClick -> {
                setUiNavigation(ToScenarioDetail(event.scenario))
            }

            is ScenariosUiEvent.OnSearchClick -> {
                setUiNavigation(ToSearch(null))
            }

            is ScenariosUiEvent.SetGuideRead -> setReadGuide()

            is ScenariosUiEvent.OnWelcomeMessageDismiss -> {
                if (event.dontShowAgain) {
                    saveWelcomeMessage(currentState.welcomeMessage?.copy(isReadByUser = true))
                }
                setState { copy(welcomeMessage = null) }
            }
        }
    }

    private fun getHome() {
        dLog("getHome")
        setState { copy(isRefreshing = true) }
        viewModelScope.launch {
            getHomeUseCase().collect {
                it.onSuccess {
                    setState {
                        copy(
                            isRefreshing = false,
                            errorHappened = false,
                            homeItems = it.toImmutableList()
                        )
                    }
                }.onFailure { apiError ->
                    setState { copy(isRefreshing = false, errorHappened = true) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }
}

