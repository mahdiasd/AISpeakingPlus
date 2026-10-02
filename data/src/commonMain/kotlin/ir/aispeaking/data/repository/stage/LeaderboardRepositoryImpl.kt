package ir.aispeaking.data.repository.stage

import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.JourneyLeaderboard
import ir.aispeaking.domain.model.stage.LeaderboardEntry
import ir.aispeaking.domain.repository.stage.LeaderboardRepository
import ir.aispeaking.network.api.stage.LeaderboardApi
import org.koin.core.annotation.Single

@Single
class LeaderboardRepositoryImpl(
    private val leaderboardApi: LeaderboardApi
) : LeaderboardRepository {

    override suspend fun getJourneyLeaderboard(page: Int, pageSize: Int): DataResult<JourneyLeaderboard> {
        val result = safeCall { leaderboardApi.getJourneyLeaderboard(page, pageSize) }
        if (result !is DataResult.Success) {
            return DataResult.Failure((result as DataResult.Failure).appError)
        }

        val data = result.data
        val currentUserRank = data.currentUserRank?.let {
            LeaderboardEntry(
                rank = it.rank,
                userId = it.userId,
                displayName = it.displayName,
                avatarUrl = it.avatarUrl,
                totalStars = it.totalStars,
                completedStages = it.completedStages
            )
        }

        val items = data.items.map {
            LeaderboardEntry(
                rank = it.rank,
                userId = it.userId,
                displayName = it.displayName,
                avatarUrl = it.avatarUrl,
                totalStars = it.totalStars,
                completedStages = it.completedStages
            )
        }

        return DataResult.Success(
            JourneyLeaderboard(
                currentUserRank = currentUserRank,
                items = items,
                page = data.page,
                pageSize = data.pageSize,
                totalCount = data.totalCount
            )
        )
    }
}
