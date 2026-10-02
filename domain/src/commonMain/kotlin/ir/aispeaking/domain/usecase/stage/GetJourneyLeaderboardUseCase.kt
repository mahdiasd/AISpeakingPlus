package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.JourneyLeaderboard
import ir.aispeaking.domain.repository.stage.LeaderboardRepository
import org.koin.core.annotation.Factory

@Factory
class GetJourneyLeaderboardUseCase(
    private val repository: LeaderboardRepository
) {
    suspend operator fun invoke(page: Int = 1, pageSize: Int = 20): DataResult<JourneyLeaderboard> =
        repository.getJourneyLeaderboard(page, pageSize)
}
