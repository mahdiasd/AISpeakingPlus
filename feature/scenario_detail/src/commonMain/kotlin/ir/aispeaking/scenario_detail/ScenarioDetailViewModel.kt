package ir.aispeaking.scenario_detail

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.model.level.LanguageGroupLevel
import ir.aispeaking.domain.model.level.copy
import ir.aispeaking.domain.model.level.getGroup
import ir.aispeaking.domain.usecase.challenge.GetChallengeDetailUseCase
import ir.aispeaking.domain.usecase.scenario.GetScenarioUseCase
import ir.aispeaking.domain.usecase.scenario.SaveScenarioToSharedUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.sharedui.Res
import ir.aispeaking.sharedui.error_language_level_click_when_progress_is_null
import ir.aispeaking.sharedui.error_max_length
import ir.aispeaking.sharedui.ui.extension.name
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.model.ui_message.UiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class ScenarioDetailViewModel(
    private val scenarioId: String,
    private val scenarioImageUrl: String,
    private val scenarioTitle: String,
    private val screenMode: ScreenMode,
    private val getScenarioUseCase: GetScenarioUseCase,
    private val getChallengeDetailUseCase: GetChallengeDetailUseCase,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
    private val saveScenarioToSharedUseCase: SaveScenarioToSharedUseCase
) : BaseViewModel<ScenarioDetailUiState, ScenarioDetailUiEvent>() {

    init {
        getSharedPrefUser()
        // Hydrate state from the route arguments so the toolbar / header render
        // immediately while the detail is being fetched.
        setState {
            copy(
                scenarioTitle = this@ScenarioDetailViewModel.scenarioTitle,
                scenarioImageUrl = this@ScenarioDetailViewModel.scenarioImageUrl.ifBlank { null },
                screenMode = this@ScenarioDetailViewModel.screenMode
            )
        }
        if (scenarioId.isNotBlank()) {
            getScenarioDetail()
        } else {
            setUiMessage(UiMessage(intValue = Res.string.error_max_length))
        }
    }

    private fun getSharedPrefUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect {
                it.onSuccess { localUser ->
                    setState {
                        copy(
                            user = localUser,
                            languageGroupLevels = LanguageGroupLevel.entries
                        )
                    }
                }
            }
        }
    }

    private fun updateLanguageGroup(selectedGroup: LanguageGroupLevel) {
        val haveAccess = currentState.scenarioDetail?.progress != null
        val temp = currentState.languageGroupLevels?.map { group ->
            if (group.name() == selectedGroup.name()) {
                group.copy(selected = true, haveAccess = true)
            } else group.copy(selected = false, haveAccess = haveAccess)
        }
        setState { copy(languageGroupLevels = temp?.toImmutableList()) }
    }

    override fun createInitialState() = ScenarioDetailUiState()

    override fun onTriggerEvent(event: ScenarioDetailUiEvent) {
        when (event) {
            ScenarioDetailUiEvent.OnBackClick -> {
                setUiNavigation(ScenarioDetailUiNavigation.ToBack)
            }

            ScenarioDetailUiEvent.OnPlayClick -> {
                when {
                    currentState.user == null -> setUiNavigation(ScenarioDetailUiNavigation.ToLogin)
                    currentState.scenarioDetail?.userHaveSubscription != true -> setUiNavigation(ScenarioDetailUiNavigation.ToSubscription)
                    currentState.scenarioDetail != null -> {
                        viewModelScope.launch {
                            saveScenarioToSharedUseCase(
                                currentState.scenarioDetail!!.scenario
                                    .copy(isChallenge = currentState.screenMode == ScreenMode.Challenge)
                            )
                            setUiNavigation(ScenarioDetailUiNavigation.ToChat(languageGroupLevel = currentState.languageGroupLevels!!.find { it.selected }!!))
                        }
                    }

                    else -> {}
                }
            }

            ScenarioDetailUiEvent.OnRetry -> {
                if (scenarioId.isBlank())
                    setUiMessage(UiMessage(intValue = Res.string.error_max_length))
                else
                    getScenarioDetail()
            }

            is ScenarioDetailUiEvent.OnLanguageGroupLevel -> {
                if (!event.languageGroupLevel.haveAccess) {
                    setUiMessage(
                        UiMessage(intValue = Res.string.error_language_level_click_when_progress_is_null),
                        delay = 5 * 1000
                    )
                } else {
                    updateLanguageGroup(event.languageGroupLevel)
                }
            }
        }
    }

    private fun getScenarioDetail() {
        setState { copy(isPageLoading = true) }
        viewModelScope.launch {
            when (currentState.screenMode) {
                is ScreenMode.Scenario -> getScenarioUseCase(id = scenarioId)
                is ScreenMode.Challenge -> getChallengeDetailUseCase(challengeId = scenarioId)
            }.collect {
                it.onSuccess {
                    setState { copy(isPageLoading = false, scenarioDetail = it) }
                    currentState.user?.let { user ->
                        updateLanguageGroup(user.languageLevel.getGroup())
                    }
                }.onFailure { apiError ->
                    setState { copy(isPageLoading = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }
}


