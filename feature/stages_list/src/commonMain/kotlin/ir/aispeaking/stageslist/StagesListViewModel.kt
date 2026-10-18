package ir.aispeaking.stageslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.stage.GetStagesUseCase
import ir.aispeaking.utils.analytics.AnalyticsTracker
import ir.aispeaking.utils.analytics.AnalyticsEvents
import ir.aispeaking.utils.analytics.logStageLockedClick
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

@Factory
class StagesListViewModel(
    private val getStagesUseCase: GetStagesUseCase,
    private val getCurrentAccessTierUseCase: GetCurrentAccessTierUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(StagesListUiState())
    val uiState: StateFlow<StagesListUiState> = _uiState.asStateFlow()

    init {
        loadStages()
    }

    fun loadStages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val tier = getCurrentAccessTierUseCase()
            when (val result = getStagesUseCase(tier)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            stages = result.data.sortedBy { stage -> stage.orderIndex },
                            errorMessage = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "خطا در دریافت مراحل. لطفاً دوباره تلاش کنید."
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: StagesListUiAction) {
        when (action) {
            is StagesListUiAction.StageClicked -> {
                if (action.stage.lockStatus == ir.aispeaking.domain.model.stage.StageLockStatus.UNLOCKED) {
                    analyticsTracker.logEvent(
                        AnalyticsEvents.EVENT_STAGE_OPENED,
                        mapOf(
                            AnalyticsEvents.PARAM_STAGE_ID to action.stage.id,
                            AnalyticsEvents.PARAM_STAGE_TITLE to action.stage.title,
                            AnalyticsEvents.PARAM_STAGE_ORDER to action.stage.orderIndex
                        )
                    )
                } else {
                    analyticsTracker.logStageLockedClick(
                        stageId = action.stage.id,
                        stageTitle = action.stage.title,
                        stageOrder = action.stage.orderIndex,
                        lockStatus = action.stage.lockStatus.name
                    )
                }
                if (action.stage.lockStatus == ir.aispeaking.domain.model.stage.StageLockStatus.LOCKED_PREVIOUS_STAGE) {
                    _uiState.update {
                        it.copy(errorMessage = "برای باز شدن این مرحله، ابتدا مرحله قبلی را تکمیل کنید.")
                    }
                }
            }
            is StagesListUiAction.RefreshRequested -> {
                loadStages()
            }
            is StagesListUiAction.ClearError -> {
                _uiState.update { it.copy(errorMessage = null) }
            }
        }
    }
}
