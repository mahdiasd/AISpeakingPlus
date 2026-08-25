package ir.aispeaking.network.dto.word

import kotlinx.serialization.Serializable

@Serializable
data class DailyWordResponse(
    val uid: String,
    val word: String,
    val options: List<String>,
    val answerIndex: Int,
    val createdAt: String,
    val expiredAt: String,
    val points: Int,
    val wordProgress: WordProgressResponse? = null,
)
