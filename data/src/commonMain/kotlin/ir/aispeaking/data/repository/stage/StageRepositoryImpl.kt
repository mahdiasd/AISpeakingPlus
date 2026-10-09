package ir.aispeaking.data.repository.stage

import ir.aispeaking.data.mapper.stage.toDomain
import ir.aispeaking.data.source.LocalGuestProgressDataSource
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.EvaluationSession
import ir.aispeaking.domain.model.stage.GrammarErrorDetail
import ir.aispeaking.domain.model.stage.HintSuggestion
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageProgress
import ir.aispeaking.domain.repository.stage.StageRepository
import ir.aispeaking.network.api.stage.StageApi
import ir.aispeaking.network.model.stage.dto.ChatMessageDto
import ir.aispeaking.network.model.stage.dto.EvaluationRequestDto
import ir.aispeaking.network.model.stage.dto.GrammarErrorDto
import ir.aispeaking.network.model.stage.dto.HintRequestDto
import org.koin.core.annotation.Single

@Single
class StageRepositoryImpl(
    private val stageApi: StageApi,
    private val localGuestProgressDataSource: LocalGuestProgressDataSource
) : StageRepository {

    override suspend fun getStages(tier: AccessTier): DataResult<List<Stage>> {
        val networkResult = safeCall { stageApi.getStages() }
        if (networkResult !is DataResult.Success) return DataResult.Failure((networkResult as DataResult.Failure).appError)

        val stages = networkResult.data.stages.map { it.toDomain() }

        // If in guest mode, merge local guest progress for stage 1
        if (tier == AccessTier.GUEST) {
            val localList = localGuestProgressDataSource.getProgressList()
            val merged = stages.map { stage ->
                val local = localList.find { it.stageId == stage.id }
                if (local != null) {
                    val progress = StageProgress(
                        id = "local-guest",
                        userId = "guest",
                        stageId = stage.id,
                        stars = local.stars,
                        bestScore = local.score,
                        repeatCount = 1,
                        completedAt = local.completedAt,
                        updatedAt = local.completedAt
                    )
                    stage.copy(userProgress = progress)
                } else {
                    stage
                }
            }
            return DataResult.Success(merged)
        }

        return DataResult.Success(stages)
    }

    override suspend fun getStageDetail(stageId: String, tier: AccessTier): DataResult<Stage> {
        val networkResult = safeCall { stageApi.getStageDetail(stageId) }
        if (networkResult !is DataResult.Success) return DataResult.Failure((networkResult as DataResult.Failure).appError)

        val stage = networkResult.data.toDomain()
        if (tier == AccessTier.GUEST) {
            val local = localGuestProgressDataSource.getProgressList().find { it.stageId == stageId }
            if (local != null) {
                val progress = StageProgress(
                    id = "local-guest",
                    userId = "guest",
                    stageId = stage.id,
                    stars = local.stars,
                    bestScore = local.score,
                    repeatCount = 1,
                    completedAt = local.completedAt,
                    updatedAt = local.completedAt
                )
                return DataResult.Success(stage.copy(userProgress = progress))
            }
        }
        return DataResult.Success(stage)
    }

    override suspend fun saveLocalGuestProgress(progress: LocalGuestProgress): DataResult<Unit> {
        localGuestProgressDataSource.saveProgress(progress)
        return DataResult.Success(Unit)
    }

    override suspend fun getLocalGuestProgress(): DataResult<List<LocalGuestProgress>> {
        return DataResult.Success(localGuestProgressDataSource.getProgressList())
    }

    override suspend fun clearLocalGuestProgress(): DataResult<Unit> {
        localGuestProgressDataSource.clear()
        return DataResult.Success(Unit)
    }

    override suspend fun requestHint(
        stageId: String,
        messages: List<Pair<String, String>>
    ): DataResult<HintSuggestion> {
        val requestDto = HintRequestDto(
            messages = messages.map { ChatMessageDto(it.first, it.second) }
        )
        val result = safeCall { stageApi.getHint(stageId, requestDto) }
        if (result !is DataResult.Success) return DataResult.Failure((result as DataResult.Failure).appError)
        return DataResult.Success(
            HintSuggestion(
                suggestionEn = result.data.suggestionEn,
                explanationFa = result.data.explanationFa
            )
        )
    }

    override suspend fun submitEvaluation(
        stageId: String,
        hintsUsedCount: Int,
        turnsCount: Int,
        transcript: List<Pair<String, String>>,
        grammarErrorsCount: Int,
        grammarErrors: List<GrammarErrorDetail>
    ): DataResult<EvaluationSession> {
        val requestDto = EvaluationRequestDto(
            hintsUsedCount = hintsUsedCount,
            turnsCount = turnsCount,
            grammarErrorsCount = grammarErrorsCount,
            grammarErrors = grammarErrors.map {
                GrammarErrorDto(it.original, it.correction, it.explanationFa)
            },
            transcript = transcript.map { ChatMessageDto(it.first, it.second) }
        )
        val result = safeCall { stageApi.submitEvaluation(stageId, requestDto) }
        if (result !is DataResult.Success) return DataResult.Failure((result as DataResult.Failure).appError)
        val d = result.data
        return DataResult.Success(
            EvaluationSession(
                stageId = d.stageId,
                hintsUsedCount = d.hintsUsedCount,
                grammarErrorsCount = d.grammarErrorsCount,
                objectiveCompleted = d.objectiveCompleted,
                grammarErrors = d.grammarErrors.map {
                    GrammarErrorDetail(it.original, it.correction, it.explanationFa)
                },
                calculatedStars = d.starsEarned,
                score = d.score,
                isHighScore = d.isHighScore,
                unlockedNextStage = d.unlockedNextStage,
                feedbackFa = d.feedbackFa
            )
        )
    }

    override suspend fun sendStageChatMessage(
        stageId: String,
        userMessage: String?,
        history: List<Pair<String, String>>
    ): DataResult<ir.aispeaking.domain.model.chat.StageChatTurnResult> {
        val requestDto = ir.aispeaking.network.model.stage.dto.StageChatRequestDto(
            message = userMessage,
            history = history.map { ChatMessageDto(it.first, it.second) }
        )
        val result = safeCall { stageApi.sendStageChatMessage(stageId, requestDto) }
        if (result !is DataResult.Success) return DataResult.Failure((result as DataResult.Failure).appError)
        val d = result.data
        return DataResult.Success(
            ir.aispeaking.domain.model.chat.StageChatTurnResult(
                message = d.message,
                translatedMessage = d.translatedMessage,
                audioUrl = d.audioUrl,
                grammarFeedbackFa = d.grammarFeedbackFa,
                objectiveCompleted = d.objectiveCompleted,
                finishTaskIndexes = d.finishTaskIndexes
            )
        )
    }
}
