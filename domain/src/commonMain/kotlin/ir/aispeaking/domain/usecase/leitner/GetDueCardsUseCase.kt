package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import org.koin.core.annotation.Factory

@Factory
class GetDueCardsUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(limit: Int = 20): DataResult<List<Flashcard>> {
        return repository.getDueCards(limit)
    }
}
