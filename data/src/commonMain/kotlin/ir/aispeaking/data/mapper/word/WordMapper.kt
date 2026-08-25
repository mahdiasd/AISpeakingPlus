package ir.aispeaking.data.mapper.word

import ir.aispeaking.domain.model.word.DailyWord
import ir.aispeaking.domain.model.word.DailyWordProgress
import ir.aispeaking.network.dto.word.DailyWordResponse
import ir.aispeaking.network.dto.word.WordProgressResponse
import kotlinx.collections.immutable.toImmutableList

fun DailyWordResponse.toDomain(): DailyWord {
    return DailyWord(
        uid = uid,
        word = word,
        options = options.toImmutableList(),
        answerIndex = answerIndex,
        createdAt = createdAt,
        expiredAt = expiredAt,
        wordProgress = wordProgress?.toDomain(),
        points = points
    )
}

fun WordProgressResponse.toDomain(): DailyWordProgress {
    return DailyWordProgress(
        uid = uid,
        selectedOptionIndex = selectedOptionIndex,
        score = score,
        isCorrect = isCorrect
    )
}