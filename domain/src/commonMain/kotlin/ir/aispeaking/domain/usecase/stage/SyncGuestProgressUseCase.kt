package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.StageProgress
import ir.aispeaking.domain.repository.stage.StageProgressRepository
import org.koin.core.annotation.Factory

@Factory
class SyncGuestProgressUseCase(
    private val repository: StageProgressRepository
) {
    suspend operator fun invoke(): DataResult<List<StageProgress>> = repository.syncGuestProgress()
}
