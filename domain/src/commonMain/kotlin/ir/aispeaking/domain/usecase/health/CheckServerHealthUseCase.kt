package ir.aispeaking.domain.usecase.health

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.health.HealthRepository
import org.koin.core.annotation.Factory

@Factory
class CheckServerHealthUseCase(
    private val healthRepository: HealthRepository
) {
    suspend operator fun invoke(): DataResult<Boolean> {
        return healthRepository.checkHealth()
    }
}
