package ir.aispeaking.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.getErrorMessage
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.JourneyLeaderboard
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.domain.usecase.auth.GetCurrentAccessTierUseCase
import ir.aispeaking.domain.usecase.auth.SendOtpUseCase
import ir.aispeaking.domain.usecase.auth.VerifyOtpUseCase
import ir.aispeaking.domain.usecase.stage.GetJourneyLeaderboardUseCase
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
    val isRegisterLoading: Boolean = false,
    val registerStep: Int = 1,
    val registerError: String? = null,
    val showLeaderboardSheet: Boolean = false,
    val leaderboard: JourneyLeaderboard? = null,
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
    private val getCurrentAccessTierUseCase: GetCurrentAccessTierUseCase,
    private val sendOtpUseCase: SendOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val getJourneyLeaderboardUseCase: GetJourneyLeaderboardUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JourneyMapUiState())
    val uiState: StateFlow<JourneyMapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val tier = getCurrentAccessTierUseCase()
            loadStages(tier)
        }
    }

    private fun normalizeDigits(input: String): String = buildString(input.length) {
        for (ch in input.trim()) {
            when (ch) {
                in '۰'..'۹' -> append('0' + (ch - '۰'))
                in '٠'..'٩' -> append('0' + (ch - '٠'))
                else -> append(ch)
            }
        }
    }

    fun sendRegisterOtp(phone: String) {
        val cleanPhone = normalizeDigits(phone)
        if (cleanPhone.length != 11 || !cleanPhone.startsWith("09")) {
            _uiState.update { it.copy(registerError = "شماره موبایل باید ۱۱ رقم و با ۰۹ شروع شود.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isRegisterLoading = true, registerError = null) }
            when (val result = sendOtpUseCase(cleanPhone)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            registerStep = 2,
                            registerError = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    val msg = result.appError.getErrorMessage().ifBlank { "خطا در ارسال کد تایید" }
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            registerError = msg
                        )
                    }
                }
            }
        }
    }

    fun verifyRegisterOtp(phone: String, otp: String) {
        val cleanPhone = normalizeDigits(phone)
        val cleanOtp = normalizeDigits(otp)
        if (cleanOtp.length < 4) {
            _uiState.update { it.copy(registerError = "کد تایید را کامل وارد کن.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isRegisterLoading = true, registerError = null) }
            when (val result = verifyOtpUseCase(cleanPhone, cleanOtp)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            registerStep = 1,
                            registerError = null
                        )
                    }
                    onRegisterSuccess()
                }
                is DataResult.Failure -> {
                    val msg = result.appError.getErrorMessage().ifBlank { "کد تایید اشتباه است یا منقضی شده است" }
                    _uiState.update {
                        it.copy(
                            isRegisterLoading = false,
                            registerError = msg
                        )
                    }
                }
            }
        }
    }

    fun resetRegisterStep() {
        _uiState.update { it.copy(registerStep = 1, registerError = null, isRegisterLoading = false) }
    }

    fun openLeaderboard() {
        viewModelScope.launch {
            when (val result = getJourneyLeaderboardUseCase()) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            leaderboard = result.data,
                            showLeaderboardSheet = true
                        )
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(snackbarMessage = "خطا در بارگذاری جدول رده‌بندی")
                    }
                }
            }
        }
    }

    fun dismissLeaderboard() {
        _uiState.update { it.copy(showLeaderboardSheet = false) }
    }

    fun onRegisterSuccess() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedStageForRegister = null, registerStep = 1, registerError = null) }
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
                _uiState.update { it.copy(selectedStageForRegister = stage, registerStep = 1, registerError = null) }
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
        _uiState.update { it.copy(selectedStageForRegister = null, registerStep = 1, registerError = null, isRegisterLoading = false) }
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
