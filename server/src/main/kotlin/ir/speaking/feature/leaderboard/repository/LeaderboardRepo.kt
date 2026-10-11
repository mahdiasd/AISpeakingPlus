package ir.speaking.feature.leaderboard.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.leaderboard.dto.CurrentUserRank
import ir.speaking.feature.leaderboard.dto.LeaderboardItem
import ir.speaking.feature.leaderboard.dto.LeaderboardResponse
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*

@Single
class LeaderboardRepo {

    private fun resolveDisplayName(userRow: org.jetbrains.exposed.sql.ResultRow?, userId: UUID): String {
        if (userRow != null) {
            val fullName = listOfNotNull(userRow[UserTable.firstName], userRow[UserTable.lastName])
                .joinToString(" ")
                .trim()
            if (fullName.isNotBlank()) return fullName
            val nick = userRow[UserTable.nickName].trim()
            if (nick.isNotBlank() && !nick.equals("Learner", ignoreCase = true)) return nick
        }
        return "Learner_${userId.toString().takeLast(4)}"
    }

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

        val neededUserIds = (pagedList.map { it.first } + listOfNotNull(currentUserId)).distinct()
        val userMap = if (neededUserIds.isNotEmpty()) {
            UserTable.selectAll()
                .where { UserTable.id inList neededUserIds }
                .associateBy { it[UserTable.id].value }
        } else {
            emptyMap()
        }

        val items = pagedList.mapIndexed { index, (userId, _, stats) ->
            val uRow = userMap[userId]
            LeaderboardItem(
                rank = fromIndex + index + 1,
                userId = userId.toString(),
                displayName = resolveDisplayName(uRow, userId),
                avatarUrl = uRow?.get(UserTable.avatar)?.takeIf { it.isNotBlank() },
                totalStars = stats.first,
                completedStages = stats.second
            )
        }

        val currentUserRank = currentUserId?.let { uid ->
            val rankIndex = rankedList.indexOfFirst { it.first == uid }
            if (rankIndex >= 0) {
                val found = rankedList[rankIndex]
                val uRow = userMap[uid]
                CurrentUserRank(
                    rank = rankIndex + 1,
                    userId = uid.toString(),
                    displayName = resolveDisplayName(uRow, uid),
                    avatarUrl = uRow?.get(UserTable.avatar)?.takeIf { it.isNotBlank() },
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

