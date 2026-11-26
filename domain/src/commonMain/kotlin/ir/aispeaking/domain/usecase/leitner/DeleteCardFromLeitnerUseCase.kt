package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.error.NetworkError
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import org.koin.core.annotation.Factory

@Factory
class DeleteCardFromLeitnerUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(cardId: String): DataResult<Boolean> {
        val cleanId = cardId.trim()
        if (cleanId.isBlank()) {
            return DataResult.Failure(NetworkError.BadRequest(message = "شناسه کارت نامعتبر است"))
        }
        return repository.deleteCard(cleanId)
    }
}
