package ir.aispeaking.domain.usecase.stage

import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.AccessTier
import ir.aispeaking.domain.model.stage.Stage
import ir.aispeaking.domain.model.stage.StageLockStatus
import ir.aispeaking.domain.model.stage.StageProgress
import ir.aispeaking.domain.repository.stage.StageRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetStagesUseCaseTest {

    private val fakeStages = listOf(
        Stage(
            id = "stage-1",
            orderIndex = 1,
            title = "Stage 1",
            titleFa = "مرحله ۱",
            briefing = "Briefing 1",
            briefingFa = "شرح ۱",
            targetObjective = "Objective 1",
            backgroundUrl = "/bg1.webp",
            characterName = "Sarah",
            lockStatus = StageLockStatus.UNLOCKED
        ),
        Stage(
            id = "stage-2",
            orderIndex = 2,
            title = "Stage 2",
            titleFa = "مرحله ۲",
            briefing = "Briefing 2",
            briefingFa = "شرح ۲",
            targetObjective = "Objective 2",
            backgroundUrl = "/bg2.webp",
            characterName = "James",
            lockStatus = StageLockStatus.LOCKED_REGISTRATION
        ),
        Stage(
            id = "stage-3",
            orderIndex = 3,
            title = "Stage 3",
            titleFa = "مرحله ۳",
            briefing = "Briefing 3",
            briefingFa = "شرح ۳",
            targetObjective = "Objective 3",
            backgroundUrl = "/bg3.webp",
            characterName = "Davies",
            lockStatus = StageLockStatus.LOCKED_SUBSCRIPTION
        )
    )

    private fun createRepository(stages: List<Stage> = fakeStages): StageRepository {
        return object : StageRepository {
            override suspend fun getStages(tier: AccessTier): DataResult<List<Stage>> {
                return DataResult.Success(stages)
            }
            override suspend fun getStageDetail(stageId: String, tier: AccessTier): DataResult<Stage> =
                DataResult.Success(stages.first { it.id == stageId })
            override suspend fun saveLocalGuestProgress(progress: ir.aispeaking.domain.model.stage.LocalGuestProgress): DataResult<Unit> =
                DataResult.Success(Unit)
            override suspend fun getLocalGuestProgress(): DataResult<List<ir.aispeaking.domain.model.stage.LocalGuestProgress>> =
                DataResult.Success(emptyList())
            override suspend fun clearLocalGuestProgress(): DataResult<Unit> = DataResult.Success(Unit)
            override suspend fun requestHint(stageId: String, messages: List<Pair<String, String>>): DataResult<ir.aispeaking.domain.model.stage.HintSuggestion> =
                DataResult.Success(ir.aispeaking.domain.model.stage.HintSuggestion("Hello", "سلام"))
            override suspend fun sendStageChatMessage(
                stageId: String,
                userMessage: String?,
                history: List<Pair<String, String>>
            ): DataResult<ir.aispeaking.domain.model.chat.StageChatTurnResult> =
                DataResult.Failure(ir.aispeaking.domain.model.error.NetworkError.Unknown())
            override suspend fun submitEvaluation(
                stageId: String,
                hintsUsedCount: Int,
                turnsCount: Int,
                transcript: List<Pair<String, String>>
            ): DataResult<ir.aispeaking.domain.model.stage.EvaluationSession> =
                DataResult.Failure(ir.aispeaking.domain.model.error.NetworkError.Unknown())
        }
    }

    @Test
    fun testGuestModeLocksStage2AndStage3() = runTest {
        val repo = createRepository()
        val useCase = GetStagesUseCase(repo)

        val result = useCase(AccessTier.GUEST)
        assertIs<DataResult.Success<List<Stage>>>(result)

        val stages = result.data
        assertEquals(StageLockStatus.UNLOCKED, stages[0].lockStatus)
        assertEquals(StageLockStatus.LOCKED_REGISTRATION, stages[1].lockStatus)
        assertEquals(StageLockStatus.LOCKED_SUBSCRIPTION, stages[2].lockStatus)
    }

    @Test
    fun testRegisteredUserStageUnlockProgression() = runTest {
        // Stage 1 completed with 2 stars
        val stage1Progress = StageProgress(
            id = "prog-1",
            userId = "user-1",
            stageId = "stage-1",
            stars = 2,
            bestScore = 80,
            repeatCount = 1,
            completedAt = "2026-09-23",
            updatedAt = "2026-09-23"
        )
        val stagesWithProgress = fakeStages.toMutableList().apply {
            this[0] = this[0].copy(userProgress = stage1Progress)
        }

        val repo = createRepository(stagesWithProgress)
        val useCase = GetStagesUseCase(repo)

        val result = useCase(AccessTier.REGISTERED_FREE)
        assertIs<DataResult.Success<List<Stage>>>(result)

        val stages = result.data
        assertEquals(StageLockStatus.UNLOCKED, stages[0].lockStatus)
        assertEquals(StageLockStatus.UNLOCKED, stages[1].lockStatus)
        assertEquals(StageLockStatus.LOCKED_SUBSCRIPTION, stages[2].lockStatus)
    }
}
