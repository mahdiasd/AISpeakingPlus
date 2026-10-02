package ir.speaking.feature.leaderboard.dto

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardItem(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalStars: Int,
    val completedStages: Int
)

@Serializable
data class CurrentUserRank(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalStars: Int,
    val completedStages: Int
)

@Serializable
data class LeaderboardResponse(
    val currentUserRank: CurrentUserRank? = null,
    val items: List<LeaderboardItem> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalCount: Long = 0
)
