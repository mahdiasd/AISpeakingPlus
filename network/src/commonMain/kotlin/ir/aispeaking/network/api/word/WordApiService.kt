package ir.aispeaking.network.api.word

import ir.aispeaking.network.dto.word.DailyWordResponse
import ir.aispeaking.network.dto.word.WordProgressResponse
import ir.aispeaking.network.model.NetworkResponse


interface WordApiService {
    suspend fun getDailyWord(): NetworkResponse<DailyWordResponse>

    suspend fun createWordProgress(
        wordId: String,
        selectedOptionIndex: Int,
        isCorrect: Boolean,
        wordScore: Int
    ): NetworkResponse<WordProgressResponse>
}