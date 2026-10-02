package ir.speaking.feature.stage.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage.dto.StageCatalogResponse
import ir.speaking.feature.stage.dto.StageDetailResponse
import ir.speaking.feature.stage.dto.StageProgressResponse
import ir.speaking.feature.stage.dto.StageSummaryResponse
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.koin.core.annotation.Single
import java.util.*

@Single
class StageRepository {

    suspend fun getStageCatalog(userId: UUID?): StageCatalogResponse = suspendTransaction {
        val allStages = StageTable.selectAll()
            .where { StageTable.status eq "PUBLISHED" }
            .orderBy(StageTable.orderIndex to SortOrder.ASC)
            .toList()

        val userProgressMap = mutableMapOf<String, StageProgressResponse>()
        var isSubscriber = false

        if (userId != null) {
            val progressRows = StageProgressTable.selectAll()
                .where { StageProgressTable.userId eq userId }
                .toList()

            for (row in progressRows) {
                val stageId = row[StageProgressTable.stageId].value
                userProgressMap[stageId] = StageProgressResponse(
                    stars = row[StageProgressTable.stars],
                    bestScore = row[StageProgressTable.bestScore],
                    repeatCount = row[StageProgressTable.repeatCount],
                    completedAt = row[StageProgressTable.completedAt].toString()
                )
            }

            val now = Clock.System.now()
            val subRow = SubscriptionTable.selectAll()
                .where {
                    (SubscriptionTable.userId eq userId) and
                    (SubscriptionTable.status eq "ACTIVE") and
                    (SubscriptionTable.expiresAt greater now)
                }
                .firstOrNull()

            isSubscriber = subRow != null
        }

        val tier = when {
            userId == null -> "GUEST"
            isSubscriber -> "SUBSCRIBER"
            else -> "REGISTERED_FREE"
        }

        val stageSummaries = allStages.mapIndexed { index, row ->
            val stageId = row[StageTable.id].value
            val orderIndex = row[StageTable.orderIndex]
            val progress = userProgressMap[stageId]

            val lockStatus = resolveLockStatus(
                index = index,
                orderIndex = orderIndex,
                tier = tier,
                allStages = allStages,
                progressMap = userProgressMap
            )

            StageSummaryResponse(
                id = stageId,
                orderIndex = orderIndex,
                title = row[StageTable.title],
                titleFa = row[StageTable.titleFa],
                briefingFa = row[StageTable.briefingFa],
                targetObjectiveFa = row[StageTable.targetObjectiveFa],
                backgroundUrl = row[StageTable.backgroundUrl],
                characterName = row[StageTable.characterName],
                characterAvatarUrl = row[StageTable.characterAvatarUrl],
                characterGender = row[StageTable.characterGender],
                initialSpeaker = row[StageTable.initialSpeaker],
                maxTurns = row[StageTable.maxTurns],
                lockStatus = lockStatus,
                progress = progress
            )
        }

        StageCatalogResponse(
            currentTier = tier,
            stages = stageSummaries
        )
    }

    suspend fun getStageDetail(stageId: String, userId: UUID?): StageDetailResponse? = suspendTransaction {
        val row = StageTable.selectAll()
            .where { (StageTable.id eq stageId) and (StageTable.status eq "PUBLISHED") }
            .firstOrNull() ?: return@suspendTransaction null

        val allStages = StageTable.selectAll()
            .where { StageTable.status eq "PUBLISHED" }
            .orderBy(StageTable.orderIndex to SortOrder.ASC)
            .toList()

        val userProgressMap = mutableMapOf<String, StageProgressResponse>()
        var isSubscriber = false

        if (userId != null) {
            val progressRows = StageProgressTable.selectAll()
                .where { StageProgressTable.userId eq userId }
                .toList()

            for (pRow in progressRows) {
                val pStageId = pRow[StageProgressTable.stageId].value
                userProgressMap[pStageId] = StageProgressResponse(
                    stars = pRow[StageProgressTable.stars],
                    bestScore = pRow[StageProgressTable.bestScore],
                    repeatCount = pRow[StageProgressTable.repeatCount],
                    completedAt = pRow[StageProgressTable.completedAt].toString()
                )
            }

            val now = Clock.System.now()
            val subRow = SubscriptionTable.selectAll()
                .where {
                    (SubscriptionTable.userId eq userId) and
                    (SubscriptionTable.status eq "ACTIVE") and
                    (SubscriptionTable.expiresAt greater now)
                }
                .firstOrNull()

            isSubscriber = subRow != null
        }

        val tier = when {
            userId == null -> "GUEST"
            isSubscriber -> "SUBSCRIBER"
            else -> "REGISTERED_FREE"
        }

        val index = allStages.indexOfFirst { it[StageTable.id].value == stageId }
        val orderIndex = row[StageTable.orderIndex]
        val lockStatus = resolveLockStatus(
            index = index,
            orderIndex = orderIndex,
            tier = tier,
            allStages = allStages,
            progressMap = userProgressMap
        )

        StageDetailResponse(
            id = stageId,
            orderIndex = orderIndex,
            title = row[StageTable.title],
            titleFa = row[StageTable.titleFa],
            briefing = row[StageTable.briefing],
            briefingFa = row[StageTable.briefingFa],
            targetObjective = row[StageTable.targetObjective],
            targetObjectiveFa = row[StageTable.targetObjectiveFa],
            characterBehavior = row[StageTable.characterBehavior],
            backgroundUrl = row[StageTable.backgroundUrl],
            characterName = row[StageTable.characterName],
            characterAvatarUrl = row[StageTable.characterAvatarUrl],
            characterGender = row[StageTable.characterGender],
            voiceId = row[StageTable.voiceId],
            initialSpeaker = row[StageTable.initialSpeaker],
            maxTurns = row[StageTable.maxTurns],
            lockStatus = lockStatus,
            progress = userProgressMap[stageId]
        )
    }

    private fun resolveLockStatus(
        index: Int,
        orderIndex: Int,
        tier: String,
        allStages: List<org.jetbrains.exposed.sql.ResultRow>,
        progressMap: Map<String, StageProgressResponse>
    ): String {
        if (orderIndex == 1) return "UNLOCKED"

        if (orderIndex == 2) {
            if (tier == "GUEST") return "LOCKED_REGISTRATION"
            val prevStageId = allStages.getOrNull(index - 1)?.get(StageTable.id)?.value
            val prevCompleted = (progressMap[prevStageId]?.stars ?: 0) >= 1
            return if (prevCompleted) "UNLOCKED" else "LOCKED_PREVIOUS_STAGE"
        }

        // Stage 3+
        if (tier != "SUBSCRIBER") return "LOCKED_SUBSCRIPTION"

        val prevStageId = allStages.getOrNull(index - 1)?.get(StageTable.id)?.value
        val prevCompleted = (progressMap[prevStageId]?.stars ?: 0) >= 1
        return if (prevCompleted) "UNLOCKED" else "LOCKED_PREVIOUS_STAGE"
    }
}
