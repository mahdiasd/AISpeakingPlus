package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.chat.StageChatTurnResult
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.repository.stage.StageRepository
import org.koin.core.annotation.Factory

@Factory
class SendStageChatMessageUseCase(
    private val stageRepository: StageRepository
) {
    suspend operator fun invoke(
        stageId: String,
        userMessage: String?,
        history: List<Pair<String, String>>
    ): DataResult<StageChatTurnResult> {
        return stageRepository.sendStageChatMessage(stageId, userMessage, history)
    }
}
