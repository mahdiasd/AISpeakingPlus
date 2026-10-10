package ir.aispeaking.domain.repository.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage

interface StageRepository {
    suspend fun getStages(tier: AccessTier): DataResult<List<Stage>>
    suspend fun getStageDetail(stageId: String, tier: AccessTier): DataResult<Stage>
    suspend fun saveLocalGuestProgress(progress: LocalGuestProgress): DataResult<Unit>
    suspend fun getLocalGuestProgress(): DataResult<List<LocalGuestProgress>>
    suspend fun clearLocalGuestProgress(): DataResult<Unit>
    suspend fun requestHint(stageId: String, messages: List<Pair<String, String>>): DataResult<ir.aispeaking.domain.model.stage.HintSuggestion>
    suspend fun submitEvaluation(
        stageId: String,
        hintsUsedCount: Int,
        turnsCount: Int,
        transcript: List<Pair<String, String>>,
        grammarErrorsCount: Int = 0,
        grammarErrors: List<ir.aispeaking.domain.model.stage.GrammarErrorDetail> = emptyList(),
        objectiveCompleted: Boolean = false
    ): DataResult<ir.aispeaking.domain.model.stage.EvaluationSession>
    suspend fun sendStageChatMessage(stageId: String, userMessage: String?, history: List<Pair<String, String>>): DataResult<ir.aispeaking.domain.model.chat.StageChatTurnResult>
}

