package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import org.koin.core.annotation.Factory

@Factory
class GetBoxCountsUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(): DataResult<Map<Int, Long>> {
        return repository.getBoxCounts()
    }
}
