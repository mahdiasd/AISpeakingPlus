package ir.speaking.feature.word.word.model

import java.time.LocalDateTime
import java.util.*

data class DailyWord(
    val uid: UUID,
    val word: String,
    val options: List<String>,
    val answerIndex: Int,
    val points: Int,
    val createdAt: LocalDateTime,
    val expiredAt: LocalDateTime,
)