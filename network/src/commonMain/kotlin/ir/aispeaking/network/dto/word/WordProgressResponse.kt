package ir.aispeaking.network.dto.word

import kotlinx.serialization.Serializable

@Serializable
data class WordProgressResponse(
    val uid: String,
    val selectedOptionIndex: Int,
    val score: Int,
    val isCorrect: Boolean,
)