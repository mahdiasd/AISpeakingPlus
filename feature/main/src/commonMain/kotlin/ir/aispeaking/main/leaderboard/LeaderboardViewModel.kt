package ir.aispeaking.main.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.JourneyLeaderboard
import ir.aispeaking.domain.usecase.stage.GetJourneyLeaderboardUseCase
import ir.aispeaking.utils.analytics.AnalyticsEvents
import ir.aispeaking.utils.analytics.AnalyticsTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Factory

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val leaderboard: JourneyLeaderboard? = null,
    val errorMessage: String? = null
)

@Factory
class LeaderboardViewModel(
    private val getJourneyLeaderboardUseCase: GetJourneyLeaderboardUseCase,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard(isRefresh: Boolean = false) {
        analyticsTracker.logEvent(AnalyticsEvents.EVENT_LEADERBOARD_OPENED)
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            } else if (_uiState.value.leaderboard == null) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            when (val result = getJourneyLeaderboardUseCase(page = 1, pageSize = 50)) {
                is DataResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            leaderboard = result.data,
                            errorMessage = null
                        )
                    }
                }
                is DataResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            errorMessage = "خطا در بارگذاری جدول رده‌بندی. لطفاً اتصال اینترنت را بررسی کنید."
                        )
                    }
                }
            }
        }
    }
}
