package ir.aispeaking.domain.repository.word

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.domain.model.word.DailyWordProgress
import kotlinx.coroutines.flow.Flow

interface WordRepository {
    suspend fun getDailyWord(): Flow<DataResult<DailyWord>>

    suspend fun createWordProgress(
        wordId: String,
        selectedOptionIndex: Int,
        isCorrect: Boolean,
        wordScore: Int
    ): Flow<DataResult<DailyWordProgress>>
}