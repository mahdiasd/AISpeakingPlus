package ir.aispeaking.domain.model.word

import kotlinx.collections.immutable.ImmutableList

data class DailyWord(
    val uid: String,
    val word: String,
    val options: ImmutableList<String>,
    val answerIndex: Int,
    val points: Int,
    val createdAt: String,
    val expiredAt: String,
    val wordProgress: DailyWordProgress? = null,
)
