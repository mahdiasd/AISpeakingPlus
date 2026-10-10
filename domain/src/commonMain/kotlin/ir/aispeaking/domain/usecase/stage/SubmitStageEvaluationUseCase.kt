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
        transcript: List<Pair<String, String>>,
        grammarErrorsCount: Int = 0,
        grammarErrors: List<ir.aispeaking.domain.model.stage.GrammarErrorDetail> = emptyList(),
        objectiveCompleted: Boolean = false
    ): DataResult<EvaluationSession> {
        return repository.submitEvaluation(
            stageId = stageId,
            hintsUsedCount = hintsUsedCount,
            turnsCount = turnsCount,
            transcript = transcript,
            grammarErrorsCount = grammarErrorsCount,
            grammarErrors = grammarErrors,
            objectiveCompleted = objectiveCompleted
        )
    }
}

