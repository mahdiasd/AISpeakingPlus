package ir.speaking.feature.word.progress.model

import kotlinx.datetime.LocalDateTime
import java.util.*

data class WordProgress(
    val uid: UUID,
    val userId: UUID,
    val wordId: UUID,
    val selectedOptionIndex: Int,
    val isCorrect: Boolean,
    val score: Int,
    val answeredAt: LocalDateTime
)