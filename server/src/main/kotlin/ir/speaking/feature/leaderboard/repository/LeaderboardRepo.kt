package ir.speaking.feature.leaderboard.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.leaderboard.dto.CurrentUserRank
import ir.speaking.feature.leaderboard.dto.LeaderboardItem
import ir.speaking.feature.leaderboard.dto.LeaderboardResponse
import ir.speaking.feature.stage_progress.db.StageProgressTable
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*

@Single
class LeaderboardRepo {

    suspend fun getLeaderboard(
        currentUserId: UUID?,
        page: Int = 1,
        pageSize: Int = 20
    ): LeaderboardResponse = suspendTransaction {
        // Compute user scores from StageProgressTable:
        // compositeScore = (total_stars * 1000) + (completed_stages * 100) + aggregate_points
        val allProgress = StageProgressTable.selectAll().toList()
        val userGroups = allProgress.groupBy { it[StageProgressTable.userId].value }

        val rankedList = userGroups.map { (userId, progressRows) ->
            val totalStars = progressRows.sumOf { it[StageProgressTable.stars] }
            val completedStages = progressRows.count { it[StageProgressTable.stars] > 0 }
            val totalPoints = progressRows.sumOf { it[StageProgressTable.bestScore] }
            val compositeScore = (totalStars * 1000L) + (completedStages * 100L) + totalPoints

            Triple(userId, compositeScore, Pair(totalStars, completedStages))
        }.sortedByDescending { it.second }

        val totalCount = rankedList.size.toLong()
        val fromIndex = ((page - 1) * pageSize).coerceAtLeast(0)
        val pagedList = if (fromIndex < rankedList.size) {
            rankedList.subList(fromIndex, (fromIndex + pageSize).coerceAtMost(rankedList.size))
        } else {
            emptyList()
        }

        val items = pagedList.mapIndexed { index, (userId, _, stats) ->
            LeaderboardItem(
                rank = fromIndex + index + 1,
                userId = userId.toString(),
                displayName = "مسافر #${userId.toString().take(4)}",
                avatarUrl = null,
                totalStars = stats.first,
                completedStages = stats.second
            )
        }

        val currentUserRank = currentUserId?.let { uid ->
            val rankIndex = rankedList.indexOfFirst { it.first == uid }
            if (rankIndex >= 0) {
                val found = rankedList[rankIndex]
                CurrentUserRank(
                    rank = rankIndex + 1,
                    userId = uid.toString(),
                    displayName = "شما",
                    avatarUrl = null,
                    totalStars = found.third.first,
                    completedStages = found.third.second
                )
            } else null
        }

        LeaderboardResponse(
            currentUserRank = currentUserRank,
            items = items,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount
        )
    }
}
