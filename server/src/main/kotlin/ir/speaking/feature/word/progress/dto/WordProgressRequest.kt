package ir.speaking.feature.word.progress.dto

import kotlinx.serialization.Serializable

@Serializable
data class WordProgressRequest(
    val wordId: String,
    val selectedOptionIndex: Int,
    val wordScore: Int,
    val isCorrect: Boolean
)