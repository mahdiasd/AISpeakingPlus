package ir.aispeaking.domain.model.placement

import ir.aispeaking.domain.model.level.LanguageLevel
import kotlinx.serialization.Serializable

@Serializable
data class PlacementQuestion(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val level: LanguageLevel,
    val category: String = "grammar"
)

@Serializable
data class PlacementQuestionsResponse(
    val sessionId: String,
    val totalQuestions: Int,
    val questions: List<PlacementQuestion>
)

@Serializable
data class QuestionReviewItem(
    val questionId: String,
    val questionText: String,
    val options: List<String>,
    val selectedOption: String? = null,
    val correctOption: String,
    val isCorrect: Boolean,
    val level: LanguageLevel
)

@Serializable
data class PlacementEvaluationResult(
    val sessionId: String,
    val assignedLevel: LanguageLevel,
    val scorePercent: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val totalCount: Int = 20,
    val levelTitleFa: String,
    val descriptionFa: String,
    val reviewItems: List<QuestionReviewItem>
)

@Serializable
data class PlacementSubmission(
    val sessionId: String,
    val answers: Map<String, String> // questionId -> selectedOption
)
