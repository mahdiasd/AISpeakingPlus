package ir.aispeaking.network.model.stage.dto

import kotlinx.serialization.Serializable

@Serializable
data class StageCatalogDataDto(
    val currentTier: String,
    val stages: List<StageSummaryDto>
)

@Serializable
data class StageSummaryDto(
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
    val progress: StageProgressDto? = null
)

@Serializable
data class StageDetailDto(
    val id: String,
    val orderIndex: Int,
    val title: String,
    val titleFa: String,
    val briefing: String,
    val briefingFa: String,
    val targetObjective: String,
    val targetObjectiveFa: String = "",
    val characterBehavior: String? = null,
    val backgroundUrl: String,
    val characterName: String,
    val characterAvatarUrl: String? = null,
    val characterGender: String = "Woman",
    val voiceId: String? = null,
    val initialSpeaker: String = "Model",
    val maxTurns: Int = 12,
    val lockStatus: String = "UNLOCKED",
    val progress: StageProgressDto? = null
)

@Serializable
data class StageProgressDto(
    val stars: Int,
    val bestScore: Int,
    val repeatCount: Int = 1,
    val completedAt: String
)

@Serializable
data class SyncProgressRequestDto(
    val items: List<SyncProgressItemDto>
)

@Serializable
data class SyncProgressItemDto(
    val stageId: String,
    val stars: Int,
    val bestScore: Int,
    val completedAt: String
)

@Serializable
data class SyncProgressItemResultDto(
    val stageId: String,
    val stars: Int,
    val bestScore: Int,
    val repeatCount: Int,
    val completedAt: String,
    val updatedAt: String
)

@Serializable
data class SyncProgressResponseDto(
    val syncedItems: List<SyncProgressItemResultDto> = emptyList()
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
)

@Serializable
data class HintRequestDto(
    val messages: List<ChatMessageDto>
)

@Serializable
data class HintResponseDto(
    val suggestionEn: String,
    val explanationFa: String
)

@Serializable
data class GrammarErrorDto(
    val original: String,
    val correction: String,
    val explanationFa: String
)

@Serializable
data class EvaluationRequestDto(
    val hintsUsedCount: Int,
    val turnsCount: Int,
    val grammarErrorsCount: Int = 0,
    val grammarErrors: List<GrammarErrorDto> = emptyList(),
    val transcript: List<ChatMessageDto>
)

@Serializable
data class EvaluationResponseDto(
    val stageId: String,
    val objectiveCompleted: Boolean,
    val grammarErrorsCount: Int,
    val hintsUsedCount: Int,
    val totalPenalties: Int,
    val starsEarned: Int,
    val score: Int,
    val isHighScore: Boolean,
    val unlockedNextStage: Boolean,
    val grammarErrors: List<GrammarErrorDto>,
    val feedbackFa: String
)

@Serializable
data class SubscriptionPlanDto(
    val id: String,
    val type: String,
    val titleFa: String,
    val durationDays: Int,
    val priceTomans: Long,
    val discountPercent: Int = 0,
    val badge: String? = null
)

@Serializable
data class SubscriptionStatusDto(
    val isSubscriber: Boolean,
    val planType: String? = null,
    val expiresAt: String? = null,
    val remainingDays: Int = 0
)

@Serializable
data class SubscribeRequestDto(
    val planId: String,
    val promoCode: String? = null
)

@Serializable
data class SubscribeResponseDto(
    val isSubscriber: Boolean,
    val planType: String,
    val expiresAt: String,
    val remainingDays: Int,
    val message: String
)

@Serializable
data class LeaderboardItemDto(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalStars: Int,
    val completedStages: Int
)

@Serializable
data class CurrentUserRankDto(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalStars: Int,
    val completedStages: Int
)

@Serializable
data class LeaderboardResponseDto(
    val currentUserRank: CurrentUserRankDto? = null,
    val items: List<LeaderboardItemDto>,
    val page: Int,
    val pageSize: Int,
    val totalCount: Long
)

@Serializable
data class StageChatRequestDto(
    val message: String? = null,
    val history: List<ChatMessageDto> = emptyList()
)

@Serializable
data class StageChatResponseDto(
    val message: String,
    val translatedMessage: String? = null,
    val audioUrl: String? = null,
    val grammarFeedbackFa: String = "",
    val objectiveCompleted: Boolean = false,
    val finishTaskIndexes: List<Int> = emptyList()
)
