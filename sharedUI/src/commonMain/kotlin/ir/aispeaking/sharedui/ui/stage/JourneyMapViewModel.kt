package ir.aispeaking.sharedui.ui.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.stage.GetStagesUseCase
import ir.aispeaking.domain.usecase.stage.SyncGuestProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

data class JourneyMapUiState(
    val isLoading: Boolean = true,
    val currentTier: AccessTier = AccessTier.GUEST,
    val stages: List<Stage> = emptyList(),
    val viewingStage: Stage? = null,
    val showPastStagesSheet: Boolean = false,
    val showPrologue: Boolean = false,
    val selectedStageForBriefing: Stage? = null,
    val selectedStageForRegister: Stage? = null,
    val selectedStageForPaywall: Stage? = null,
    val snackbarMessage: String? = null
) {
    /**
     * The stage to display on screen: either a previewed stage or the user's active stage in the story journey.
     */
    val currentStage: Stage?
        get() = viewingStage
            ?: stages.firstOrNull { (it.userProgress?.stars ?: 0) == 0 }
            ?: stages.lastOrNull()

    /**
     * Total stars earned by the user across all stages.
     */
    val totalStarsEarned: Int
        get() = stages.sumOf { it.userProgress?.stars ?: 0 }

    /**
     * Past stages: stages before current active stage or already played with stars.
     */
    val pastStages: List<Stage>
        get() {
            val activeOrder = stages.firstOrNull { (it.userProgress?.stars ?: 0) == 0 }?.orderIndex
                ?: ((stages.lastOrNull()?.orderIndex ?: 1) + 1)
            return stages.filter { it.orderIndex < activeOrder || (it.userProgress?.stars ?: 0) > 0 }
        }

    val isViewingPastStage: Boolean
        get() = viewingStage != null
}

@Factory
class JourneyMapViewModel(
    private val getStagesUseCase: GetStagesUseCase,
    private val syncGuestProgressUseCase: SyncGuestProgressUseCase,
    private val getCurrentAccessTierUseCase: GetCurrentAccessTierUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyMapUiState())
    val uiState: StateFlow<JourneyMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val tier = getCurrentAccessTierUseCase()
            loadStages(tier)
        }
    }

    fun onRegisterSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedStageForRegister = null) }
            syncGuestProgressUseCase()
            val tier = getCurrentAccessTierUseCase()
            loadStages(tier)
            _uiState.update { it.copy(snackbarMessage = "ثبت‌نام با موفقیت انجام شد و مرحله ۲ باز شد!") }
        }
    }

    fun onSubscriptionSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedStageForPaywall = null) }
            val tier = getCurrentAccessTierUseCase()
            loadStages(tier)
            _uiState.update { it.copy(snackbarMessage = "اشتراک شما فعال شد! تمام مراحل باز شدند.") }
        }
    }

    fun refreshStages() {
        viewModelScope.launch {
            val tier = getCurrentAccessTierUseCase()
            _uiState.update { it.copy(viewingStage = null) }
            loadStages(tier)
        }
    }

    fun loadStages(tier: AccessTier = AccessTier.GUEST) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentTier = tier) }
            when (val result = getStagesUseCase(tier)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            stages = result.data
                        )
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            snackbarMessage = "خطا در بارگذاری مراحل"
                        )
                    }
                }
            }
        }
    }

    fun onStageClicked(stage: Stage) {
        when (stage.lockStatus) {
            StageLockStatus.UNLOCKED -> {
                _uiState.update { it.copy(selectedStageForBriefing = stage) }
            }
            StageLockStatus.LOCKED_REGISTRATION -> {
                _uiState.update { it.copy(selectedStageForRegister = stage) }
            }
            StageLockStatus.LOCKED_SUBSCRIPTION -> {
                _uiState.update { it.copy(selectedStageForPaywall = stage) }
            }
            StageLockStatus.LOCKED_PREVIOUS_STAGE -> {
                _uiState.update { it.copy(snackbarMessage = "برای باز شدن این مرحله، مرحله قبل را تکمیل کنید.") }
            }
        }
    }

    fun onStartCurrentStage() {
        val stage = _uiState.value.currentStage ?: return
        onStageClicked(stage)
    }

    fun onSubscriptionCtaClicked() {
        val stageForPaywall = _uiState.value.stages.firstOrNull { it.lockStatus == StageLockStatus.LOCKED_SUBSCRIPTION }
            ?: _uiState.value.currentStage
            ?: _uiState.value.stages.firstOrNull()
        if (stageForPaywall != null) {
            _uiState.update { it.copy(selectedStageForPaywall = stageForPaywall) }
        }
    }

    fun showPastStages() {
        _uiState.update { it.copy(showPastStagesSheet = true) }
    }

    fun dismissPastStages() {
        _uiState.update { it.copy(showPastStagesSheet = false) }
    }

    fun selectStageToView(stage: Stage) {
        _uiState.update { it.copy(viewingStage = stage, showPastStagesSheet = false) }
    }

    fun resetToActiveStage() {
        _uiState.update { it.copy(viewingStage = null) }
    }

    fun dismissBriefing() {
        _uiState.update { it.copy(selectedStageForBriefing = null) }
    }

    fun dismissRegister() {
        _uiState.update { it.copy(selectedStageForRegister = null) }
    }

    fun dismissPaywall() {
        _uiState.update { it.copy(selectedStageForPaywall = null) }
    }

    fun dismissPrologue() {
        _uiState.update { it.copy(showPrologue = false) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}
