package ir.aispeaking.data.mapper.placement

import ir.aispeaking.domain.model.level.LanguageLevel
import ir.aispeaking.domain.model.placement.PlacementEvaluationResult
import ir.aispeaking.domain.model.placement.PlacementQuestion
import ir.aispeaking.domain.model.placement.PlacementQuestionsResponse
import ir.aispeaking.domain.model.placement.PlacementSubmission
import ir.aispeaking.domain.model.placement.QuestionReviewItem
import ir.aispeaking.network.model.placement.dto.PlacementEvaluationResponseDto
import ir.aispeaking.network.model.placement.dto.PlacementQuestionDto
import ir.aispeaking.network.model.placement.dto.PlacementQuestionsResponseDto
import ir.aispeaking.network.model.placement.dto.PlacementSubmitRequestDto
import ir.aispeaking.network.model.placement.dto.QuestionReviewDto

fun String.toLanguageLevel(): LanguageLevel {
    return try {
        LanguageLevel.valueOf(this.uppercase())
    } catch (_: Exception) {
        LanguageLevel.A1
    }
}

fun PlacementQuestionDto.toDomain(): PlacementQuestion {
    return PlacementQuestion(
        id = id,
        questionText = questionText,
        options = options,
        level = level.toLanguageLevel(),
        category = category
    )
}

fun PlacementQuestionsResponseDto.toDomain(): PlacementQuestionsResponse {
    return PlacementQuestionsResponse(
        sessionId = sessionId,
        totalQuestions = totalQuestions,
        questions = questions.map { it.toDomain() }
    )
}

fun QuestionReviewDto.toDomain(): QuestionReviewItem {
    return QuestionReviewItem(
        questionId = questionId,
        questionText = questionText,
        options = options,
        selectedOption = selectedOption,
        correctOption = correctOption,
        isCorrect = isCorrect,
        level = level.toLanguageLevel()
    )
}

fun PlacementEvaluationResponseDto.toDomain(): PlacementEvaluationResult {
    return PlacementEvaluationResult(
        sessionId = sessionId,
        assignedLevel = assignedLevel.toLanguageLevel(),
        scorePercent = scorePercent,
        correctCount = correctCount,
        wrongCount = wrongCount,
        totalCount = totalCount,
        levelTitleFa = levelTitleFa,
        descriptionFa = descriptionFa,
        reviewItems = reviewItems.map { it.toDomain() }
    )
}

fun PlacementSubmission.toDto(): PlacementSubmitRequestDto {
    return PlacementSubmitRequestDto(
        sessionId = sessionId,
        answers = answers
    )
}
