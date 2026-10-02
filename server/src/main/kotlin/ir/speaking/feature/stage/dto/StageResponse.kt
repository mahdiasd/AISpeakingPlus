package ir.speaking.feature.stage.dto

import kotlinx.serialization.Serializable

@Serializable
data class StageCatalogResponse(
    val currentTier: String,
    val stages: List<StageSummaryResponse>
)

@Serializable
data class StageSummaryResponse(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefingFa: String,
    val targetObjectiveFa: String? = null,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String? = null,
    val characterGender: String = "Woman",
    val initialSpeaker: String = "Model",
    val maxTurns: Int = 12,
    val lockStatus: String = "UNLOCKED",
    val progress: StageProgressResponse? = null
)

@Serializable
data class StageDetailResponse(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefing: String,
    val briefingFa: String,
    val targetObjective: String,
    val targetObjectiveFa: String,
    val characterBehavior: String? = null,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String? = null,
    val characterGender: String = "Woman",
    val voiceId: String? = null,
    val initialSpeaker: String = "Model",
    val maxTurns: Int = 12,
    val lockStatus: String = "UNLOCKED",
    val progress: StageProgressResponse? = null
)

@Serializable
data class StageProgressResponse(
    val stars: Int,
    val bestScore: Int,
    val repeatCount: Int = 1,
    val completedAt: String
)
