package ir.aispeaking.domain.usecase.word

import ir.aispeaking.domain.repository.word.WordRepository
import org.koin.core.annotation.Single

@Single
class CreateWordProgressUseCase(private val repo: WordRepository) {
    suspend operator fun invoke(
        wordId: String,
        selectedOptionIndex: Int,
        isCorrect: Boolean,
        wordScore: Int
    ) = repo.createWordProgress(
        wordId = wordId,
        selectedOptionIndex = selectedOptionIndex,
        isCorrect = isCorrect,
        wordScore = wordScore
    )
}