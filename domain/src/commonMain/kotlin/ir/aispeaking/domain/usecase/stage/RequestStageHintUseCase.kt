package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.HintSuggestion
import ir.aispeaking.domain.repository.stage.StageRepository
import org.koin.core.annotation.Factory

@Factory
class RequestStageHintUseCase(
    private val repository: StageRepository
) {
    suspend operator fun invoke(stageId: String, messages: List<Pair<String, String>>): DataResult<HintSuggestion> {
        return repository.requestHint(stageId, messages)
    }
}
