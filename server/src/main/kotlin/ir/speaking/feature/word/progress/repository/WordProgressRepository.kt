package ir.speaking.feature.word.progress.repository

import ir.speaking.feature.word.progress.model.WordProgress
import java.util.*

interface WordProgressRepository {
    suspend fun create(
        userId: UUID,
        wordId: UUID,
        selectedOptionIndex: Int,
        score: Int,
        isCorrect: Boolean
    ): WordProgress


    suspend fun findByUserId(userId: UUID, wordId: UUID): WordProgress?


    suspend fun countByUser(userId: UUID): Long
}