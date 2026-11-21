package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import org.koin.core.annotation.Factory

@Factory
class SubmitCardReviewUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(cardId: String, remembered: Boolean): DataResult<Flashcard> {
        return repository.submitCardReview(cardId, remembered)
    }
}
