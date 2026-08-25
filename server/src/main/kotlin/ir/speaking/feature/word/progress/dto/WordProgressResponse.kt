package ir.speaking.feature.word.progress.dto

import ir.speaking.feature.word.progress.model.WordProgress
import kotlinx.serialization.Serializable

@Serializable
data class WordProgressResponse(
    val uid: String,
    val selectedOptionIndex: Int,
    val score: Int,
    val isCorrect: Boolean,
)

fun WordProgress.toResponse(): WordProgressResponse {
    return WordProgressResponse(
        uid = uid.toString(),
        selectedOptionIndex = selectedOptionIndex,
        isCorrect = isCorrect,
        score = score,
    )
}