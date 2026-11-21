package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.leitner.Flashcard
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import org.koin.core.annotation.Factory

@Factory
class GetCardsByBoxUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(boxNumber: Int?): DataResult<List<Flashcard>> {
        return if (boxNumber == null) {
            repository.getAllCards()
        } else {
            repository.getCardsByBox(boxNumber)
        }
    }
}
