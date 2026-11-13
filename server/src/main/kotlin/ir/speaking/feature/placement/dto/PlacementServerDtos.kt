package ir.speaking.feature.placement.dto

import kotlinx.serialization.Serializable

@Serializable
data class PlacementQuestionServerDto(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val level: String,
    val category: String = "grammar"
)

@Serializable
data class PlacementQuestionsResponseServerDto(
    val sessionId: String,
    val totalQuestions: Int,
    val questions: List<PlacementQuestionServerDto>
)

@Serializable
data class PlacementSubmitRequestServerDto(
    val sessionId: String,
    val answers: Map<String, String> // questionId -> selectedOption
)

@Serializable
data class QuestionReviewServerDto(
    val questionId: String,
    val questionText: String,
    val options: List<String>,
    val selectedOption: String? = null,
    val correctOption: String,
    val isCorrect: Boolean,
    val level: String
)

@Serializable
data class PlacementEvaluationResponseServerDto(
    val sessionId: String,
    val assignedLevel: String,
    val scorePercent: Int,
    val correctCount: Int,
    val wrongCount: Int,
    val totalCount: Int = 20,
    val levelTitleFa: String,
    val descriptionFa: String,
    val reviewItems: List<QuestionReviewServerDto>
)
