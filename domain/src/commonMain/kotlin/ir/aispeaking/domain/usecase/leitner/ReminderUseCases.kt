package ir.aispeaking.domain.usecase.leitner

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.leitner.LeitnerRepository
import org.koin.core.annotation.Factory

@Factory
class GetReminderTimeUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(): DataResult<String?> {
        return repository.getReminderTime()
    }
}

@Factory
class SetReminderTimeUseCase(
    private val repository: LeitnerRepository
) {
    suspend operator fun invoke(time: String): DataResult<Boolean> {
        return repository.setReminderTime(time)
    }
}
