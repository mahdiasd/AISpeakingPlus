package ir.aispeaking.domain.usecase.word

import ir.aispeaking.domain.repository.word.WordRepository
import org.koin.core.annotation.Single

@Single
class GetDailyWordUseCase(private val repo: WordRepository) {
    suspend operator fun invoke() = repo.getDailyWord()
}