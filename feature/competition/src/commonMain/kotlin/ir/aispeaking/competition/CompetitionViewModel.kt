package ir.aispeaking.competition

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.usecase.challenge.GetChallengeUseCase
import ir.aispeaking.domain.usecase.user.GetServerUserUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.domain.usecase.user.GetTopUsersUseCase
import ir.aispeaking.domain.usecase.word.CreateWordProgressUseCase
import ir.aispeaking.domain.usecase.word.GetDailyWordUseCase
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class CompetitionViewModel(
    private val createWordProgressUseCase: CreateWordProgressUseCase,
    private val getDailyWordUseCase: GetDailyWordUseCase,
    private val getTopUsersUseCase: GetTopUsersUseCase,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
    private val getChallengeUseCase: GetChallengeUseCase,
    private val getServerUserUseCase: GetServerUserUseCase,
) : BaseViewModel<CompetitionUiState, CompetitionUiEvent>() {

    init {
        getSharedPrefUser()
        callApis()
    }

    private fun getSharedPrefUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect {
                it.onSuccess { localUser ->
                    setState { copy(user = localUser) }
                }
            }
        }
    }

    override fun createInitialState() = CompetitionUiState()

    override fun onTriggerEvent(event: CompetitionUiEvent) {
        when (event) {
            is CompetitionUiEvent.OnRetry -> {
                callApis()
            }

            is CompetitionUiEvent.OnWordSelectedAnswer -> {
                if (currentState.user != null)
                    setState { copy(userSelectedWordIndex = event.index) }
                else
                    setUiNavigation(CompetitionUiNavigation.ToLogin)
            }

            is CompetitionUiEvent.OnSendAnswer -> {
                val isCorrect = currentState.userSelectedWordIndex == currentState.dailyWord?.answerIndex
                createWordProgress(isCorrect = isCorrect, selectedIndex = currentState.userSelectedWordIndex!!)
            }

            is CompetitionUiEvent.OnNavigateToChallenge -> {
                setUiNavigation(CompetitionUiNavigation.ToChallenge(currentState.challengeSummary!!))
            }

            is CompetitionUiEvent.OnNavigateToLogin -> setUiNavigation(CompetitionUiNavigation.ToLogin)
            is CompetitionUiEvent.SyncUserWithServer -> syncUserWithServer()
            is CompetitionUiEvent.OnShowTranslateDialog -> {
                if (currentState.user != null)
                    setState { copy(showTranslateDialog = event.show) }
                else
                    setUiNavigation(CompetitionUiNavigation.ToLogin)
            }
        }
    }

    private fun createWordProgress(isCorrect: Boolean, selectedIndex: Int) {
        setState { copy(acceptableBtnLoading = true) }
        currentState.dailyWord?.let { dailyWord ->
            viewModelScope.launch {
                createWordProgressUseCase(
                    wordId = dailyWord.uid,
                    selectedOptionIndex = selectedIndex,
                    isCorrect = isCorrect,
                    wordScore = dailyWord.points
                ).collect {
                    it.onSuccess {
                        onTriggerEvent(CompetitionUiEvent.SyncUserWithServer)
                        setState {
                            copy(
                                dailyWord = dailyWord.copy(wordProgress = it),
                                acceptableBtnLoading = false
                            )
                        }
                    }.onFailure { apiError ->
                        setState { copy(dailyWord = dailyWord.copy(wordProgress = null), acceptableBtnLoading = false) }
                        setUiMessage(apiError.toUiMessage())
                    }
                }
            }
        }
    }

    private fun callApis() {
        setState { copy(isPageLoading = true) }
        viewModelScope.launch {
            combine(
                flow = getTopUsersUseCase(),
                flow2 = getDailyWordUseCase(),
                flow3 = getChallengeUseCase(),
                transform = { topUsersResult, wordResult, challengeResult ->
                    Triple(topUsersResult, wordResult, challengeResult)
                }
            ).collect { (topUsersResult, wordResult, challengeResult) ->
                setState { copy(isPageLoading = false) }
                topUsersResult.onFailure { appError ->
                    setUiMessage(appError.toUiMessage())
                }
                wordResult.onFailure { appError ->
                    setUiMessage(appError.toUiMessage())
                    return@onFailure
                }
                challengeResult.onFailure { appError ->
                    setUiMessage(appError.toUiMessage())
                    return@onFailure
                }

                topUsersResult.onSuccess { users ->
                    wordResult.onSuccess { dailyWord ->
                        challengeResult.onSuccess { challengeSummary ->
                            setState {
                                copy(
                                    users = users.toImmutableList(),
                                    dailyWord = dailyWord,
                                    challengeSummary = challengeSummary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun syncUserWithServer() {
        viewModelScope.launch {
            getServerUserUseCase().collect {
                it.onSuccess {
                    setState { copy(user = it) }
                }.onFailure { apiError ->
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }

}

