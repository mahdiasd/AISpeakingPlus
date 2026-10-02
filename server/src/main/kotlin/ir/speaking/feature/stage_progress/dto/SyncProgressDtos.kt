package ir.speaking.feature.stage_progress.dto

import kotlinx.serialization.Serializable

@Serializable
data class SyncProgressItem(
    val stageId: String,
    val stars: Int,
    val bestScore: Int,
    val completedAt: String
)

@Serializable
data class SyncProgressRequest(
    val items: List<SyncProgressItem> = emptyList()
)

typealias ProgressSyncRequest = SyncProgressRequest

@Serializable
data class SyncProgressItemResult(
    val stageId: String,
    val stars: Int,
    val bestScore: Int,
    val repeatCount: Int,
    val completedAt: String,
    val updatedAt: String
)

@Serializable
data class SyncProgressResponse(
    val syncedItems: List<SyncProgressItemResult>
)
