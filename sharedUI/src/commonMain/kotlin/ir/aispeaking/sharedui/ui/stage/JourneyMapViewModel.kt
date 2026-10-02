package ir.aispeaking.sharedui.ui.stage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
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
    val showPrologue: Boolean = true,
    val selectedStageForBriefing: Stage? = null,
    val selectedStageForRegister: Stage? = null,
    val selectedStageForPaywall: Stage? = null,
    val snackbarMessage: String? = null
)

@Factory
class JourneyMapViewModel(
    private val getStagesUseCase: GetStagesUseCase,
    private val syncGuestProgressUseCase: SyncGuestProgressUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyMapUiState())
    val uiState: StateFlow<JourneyMapUiState> = _uiState.asStateFlow()

    init {
        loadStages()
    }

    fun onRegisterSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedStageForRegister = null) }
            syncGuestProgressUseCase()
            loadStages(AccessTier.REGISTERED_FREE)
            _uiState.update { it.copy(snackbarMessage = "ثبت‌نام با موفقیت انجام شد و مرحله ۲ باز شد!") }
        }
    }

    fun onSubscriptionSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedStageForPaywall = null) }
            loadStages(AccessTier.SUBSCRIBER)
            _uiState.update { it.copy(snackbarMessage = "اشتراک شما فعال شد! تمام مراحل باز شدند.") }
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
