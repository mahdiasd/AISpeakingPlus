package ir.aispeaking.domain.model.stage

import kotlinx.serialization.Serializable

@Serializable
data class Stage(
    val id: String,
    val orderIndex: Int,
    val seasonNumber: Int = 1,
    val seasonTitleFa: String = "فصل اول: روزهای آغازین در لندن",
    val phaseNumber: Int = 1,
    val phaseTitleFa: String = "فاز ۱: ورود و استقرار اولیه",
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
    val lockStatus: StageLockStatus = StageLockStatus.UNLOCKED,
    val userProgress: StageProgress? = null
)

@Serializable
data class StageProgress(
    val id: String = "",
    val userId: String = "",
    val stageId: String,
    val stars: Int, // 0 to 3
    val bestScore: Int = 0,
    val repeatCount: Int = 1,
    val completedAt: String,
    val updatedAt: String = ""
)

@Serializable
data class LocalGuestProgress(
    val stageId: String,
    val stars: Int,
    val score: Int,
    val completedAt: String
)
