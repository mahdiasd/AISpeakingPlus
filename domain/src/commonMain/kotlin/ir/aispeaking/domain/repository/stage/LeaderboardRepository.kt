package ir.aispeaking.domain.repository.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.JourneyLeaderboard

interface LeaderboardRepository {
    suspend fun getJourneyLeaderboard(page: Int = 1, pageSize: Int = 20): DataResult<JourneyLeaderboard>
}
