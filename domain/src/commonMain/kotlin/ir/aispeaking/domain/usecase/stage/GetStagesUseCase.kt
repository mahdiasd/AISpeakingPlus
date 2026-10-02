package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.domain.repository.stage.StageRepository
import org.koin.core.annotation.Factory

@Factory
class GetStagesUseCase(
    private val repository: StageRepository
) {
    suspend operator fun invoke(tier: AccessTier): DataResult<List<Stage>> {
        val result = repository.getStages(tier)
        if (result !is DataResult.Success) return result

        val sortedStages = result.data.sortedBy { it.orderIndex }
        val stagesWithLock = sortedStages.mapIndexed { index, stage ->
            val lockStatus = resolveLockStatus(index, stage, sortedStages, tier)
            stage.copy(lockStatus = lockStatus)
        }
        return DataResult.Success(stagesWithLock)
    }

    private fun resolveLockStatus(
        index: Int,
        stage: Stage,
        allStages: List<Stage>,
        tier: AccessTier
    ): StageLockStatus {
        // Stage 1 (orderIndex 1) is always unlocked for everyone
        if (stage.orderIndex == 1) return StageLockStatus.UNLOCKED

        // Stage 2 (orderIndex 2) requires registration
        if (stage.orderIndex == 2) {
            if (tier == AccessTier.GUEST) {
                return StageLockStatus.LOCKED_REGISTRATION
            }
            // Check if stage 1 was completed (>= 1 star)
            val prevStage = allStages.getOrNull(index - 1)
            val prevCompleted = (prevStage?.userProgress?.stars ?: 0) >= 1
            return if (prevCompleted) StageLockStatus.UNLOCKED else StageLockStatus.LOCKED_PREVIOUS_STAGE
        }

        // Stage 3+ (orderIndex >= 3) requires subscription
        if (tier != AccessTier.SUBSCRIBER) {
            return StageLockStatus.LOCKED_SUBSCRIPTION
        }

        // For subscriber, check if previous stage was completed
        val prevStage = allStages.getOrNull(index - 1)
        val prevCompleted = (prevStage?.userProgress?.stars ?: 0) >= 1
        return if (prevCompleted) StageLockStatus.UNLOCKED else StageLockStatus.LOCKED_PREVIOUS_STAGE
    }
}
