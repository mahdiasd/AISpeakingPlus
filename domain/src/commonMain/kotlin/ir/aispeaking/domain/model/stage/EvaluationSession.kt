package ir.aispeaking.domain.model.stage

import kotlinx.serialization.Serializable

@Serializable
data class EvaluationSession(
    val stageId: String,
    val hintsUsedCount: Int,
    val grammarErrorsCount: Int,
    val objectiveCompleted: Boolean,
    val grammarErrors: List<GrammarErrorDetail>,
    val calculatedStars: Int,
    val score: Int = 0,
    val isHighScore: Boolean = false,
    val unlockedNextStage: Boolean = false,
    val feedbackFa: String
)

@Serializable
data class GrammarErrorDetail(
    val original: String,
    val correction: String,
    val explanationFa: String
)

@Serializable
data class HintSuggestion(
    val suggestionEn: String,
    val explanationFa: String
)
