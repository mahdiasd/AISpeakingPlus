package ir.speaking.feature.word.word.dto

import ir.speaking.feature.word.progress.dto.WordProgressResponse
import ir.speaking.feature.word.word.model.DailyWord
import kotlinx.serialization.Serializable

@Serializable
data class DailyWordResponse(
    val uid: String,
    val word: String,
    val options: List<String>,
    val answerIndex: Int,
    val createdAt: String,
    val points: Int,
    val expiredAt: String,
    val wordProgress: WordProgressResponse? = null,
)

fun DailyWord.toResponse(wordProgress: WordProgressResponse? = null): DailyWordResponse {
    return DailyWordResponse(
        uid = uid.toString(),
        word = word,
        options = options,
        answerIndex = answerIndex,
        createdAt = createdAt.toString(),
        expiredAt = expiredAt.toString(),
        points = points,
        wordProgress = wordProgress,
    )
}