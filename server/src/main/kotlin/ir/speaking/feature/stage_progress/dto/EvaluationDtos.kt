package ir.speaking.feature.stage_progress.dto

import kotlinx.serialization.Serializable

@Serializable
data class EvaluationTranscriptItem(
    val role: String,
    val content: String
)

@Serializable
data class EvaluationRequest(
    val hintsUsedCount: Int = 0,
    val turnsCount: Int = 0,
    val transcript: List<EvaluationTranscriptItem> = emptyList()
)

@Serializable
data class GrammarErrorItem(
    val original: String,
    val correction: String,
    val explanationFa: String
)

@Serializable
data class EvaluationResponse(
    val stageId: String,
    val objectiveCompleted: Boolean,
    val grammarErrorsCount: Int,
    val hintsUsedCount: Int,
    val totalPenalties: Int,
    val starsEarned: Int,
    val score: Int,
    val isHighScore: Boolean,
    val unlockedNextStage: Boolean,
    val grammarErrors: List<GrammarErrorItem>,
    val feedbackFa: String
)
