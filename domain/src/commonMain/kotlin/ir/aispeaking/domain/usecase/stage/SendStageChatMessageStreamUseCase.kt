package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.chat.StageChatStreamEvent
import ir.aispeaking.domain.repository.stage.StageRepository
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Factory

@Factory
class SendStageChatMessageStreamUseCase(
    private val stageRepository: StageRepository
) {
    operator fun invoke(
        stageId: String,
        userMessage: String?,
        history: List<Pair<String, String>>,
        userLevel: String? = null
    ): Flow<StageChatStreamEvent> {
        return stageRepository.streamStageChatMessage(stageId, userMessage, history, userLevel)
    }
}
