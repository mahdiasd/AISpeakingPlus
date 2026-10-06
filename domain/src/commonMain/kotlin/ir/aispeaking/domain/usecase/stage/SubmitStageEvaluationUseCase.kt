package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.repository.stage.StageRepository
import org.koin.core.annotation.Factory

@Factory
class SubmitStageEvaluationUseCase(
    private val repository: StageRepository
) {
    suspend operator fun invoke(
        stageId: String,
        hintsUsedCount: Int,
        turnsCount: Int,
        transcript: List<Pair<String, String>>
    ): DataResult<EvaluationSession> {
        return repository.submitEvaluation(stageId, hintsUsedCount, turnsCount, transcript)
    }
}
