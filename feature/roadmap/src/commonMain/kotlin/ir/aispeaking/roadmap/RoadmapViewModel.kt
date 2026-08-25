package ir.aispeaking.roadmap

import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.onFailure
import ir.aispeaking.domain.model.data_result.onSuccess
import ir.aispeaking.domain.usecase.user.GetServerUserUseCase
import ir.aispeaking.domain.usecase.user.GetSharedPrefUserUseCase
import ir.aispeaking.roadmap.model.LevelData
import ir.aispeaking.sharedui.ui.model.error_mapper.toUiMessage
import ir.aispeaking.sharedui.ui.viewmodel.BaseViewModel
import ir.aispeaking.utils.constant.AppConstant
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class RoadmapViewModel(
    private val getServerUserUseCase: GetServerUserUseCase,
    private val getSharedPrefUserUseCase: GetSharedPrefUserUseCase,
) : BaseViewModel<RoadmapUiState, RoadmapUiEvent>() {
    init {
        getSharedPrefUser()
    }

    private fun getSharedPrefUser() {
        viewModelScope.launch {
            getSharedPrefUserUseCase().collect {
                it.onSuccess { localUser ->
                    setState { copy(user = localUser) }
                    generateRoadMap()
                }.onFailure {
//                    setUiMessage(
//                        UiMessage(
//                            intValue = Res.string.error_local_user_not_found,
//                            messageType = MessageType.Device,
//                            status = MessageStatus.Failure
//                        )
//                    )
                }
            }
        }
    }

    private fun generateRoadMap() {
        viewModelScope.launch {
            val userScore = currentState.user!!.score + 1
            val count = (userScore / 50) + 10
            setState {
                copy(
                    levels = List(count)
                    { levelIndex ->
                        val level = levelIndex + 1
                        val min = if (level == 1) 0 else (level * 50) - 49
                        val max = (level * 50)

                        LevelData(
                            level = level,
                            title = "Level $level",
                            rangeText = "$min - $max",
                            isPassed = max <= userScore || level == 1
                        )
                    }.toImmutableList()
                )
            }
        }
    }

    override fun createInitialState() = RoadmapUiState()

    override fun onTriggerEvent(event: RoadmapUiEvent) {
        when (event) {
            is RoadmapUiEvent.FetchUser -> {
                if (AppConstant.needToFetchUser) {
                    getServerUser()
                } else {
                    getSharedPrefUser()
                }
            }
        }
    }

    private fun getServerUser() {
        viewModelScope.launch {
            getServerUserUseCase().collect {
                it.onSuccess {
                    setState { copy(isFetchingUser = false, user = it) }
                }.onFailure { apiError ->
                    setState { copy(isFetchingUser = false) }
                    setUiMessage(apiError.toUiMessage())
                }
            }
        }
    }


}

