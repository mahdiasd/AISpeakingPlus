package ir.aispeaking.domain.model.stage

import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalStars: Int,
    val completedStages: Int
)

@Serializable
data class JourneyLeaderboard(
    val currentUserRank: LeaderboardEntry? = null,
    val items: List<LeaderboardEntry> = emptyList(),
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalCount: Long = 0
)
