package ir.aispeaking.network.model.placement.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlacementQuestionDto(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val level: String,
    val category: String = "grammar"
)

@Serializable
data class PlacementQuestionsResponseDto(
    val sessionId: String,
    val totalQuestions: Int,
    val questions: List<PlacementQuestionDto>
)

@Serializable
data class PlacementSubmitRequestDto(
    val sessionId: String,
    val answers: Map<String, String>
)

@Serializable
data class QuestionReviewDto(
    val questionId: String,
    val questionText: String,
    val options: List<String>,
    val selectedOption: String? = null,
    val correctOption: String,
    val isCorrect: Boolean,
    val level: String
)

@Serializable
data class PlacementEvaluationResponseDto(
    val sessionId: String,
    val assignedLevel: String,
    val scorePercent: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val totalCount: Int = 20,
    val levelTitleFa: String,
    val descriptionFa: String,
    val reviewItems: List<QuestionReviewDto>
)
