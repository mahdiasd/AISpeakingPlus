package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.repository.stage.StageRepository
import org.koin.core.annotation.Factory

@Factory
class GetStageDetailUseCase(
    private val repository: StageRepository
) {
    suspend operator fun invoke(stageId: String, tier: AccessTier): DataResult<Stage> {
        return repository.getStageDetail(stageId, tier)
    }
}
