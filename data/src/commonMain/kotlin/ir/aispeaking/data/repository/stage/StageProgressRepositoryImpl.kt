@file:OptIn(kotlin.time.ExperimentalTime::class)
package ir.aispeaking.data.repository.stage

import ir.aispeaking.data.source.LocalGuestProgressDataSource
import ir.aispeaking.data.utils.safeCall
import ir.aispeaking.domain.model.data_result.DataResult
import ir.aispeaking.domain.model.stage.LocalGuestProgress
import ir.aispeaking.domain.model.stage.StageProgress
import ir.aispeaking.domain.repository.stage.StageProgressRepository
import ir.aispeaking.network.api.stage.StageProgressApi
import ir.aispeaking.network.model.stage.dto.SyncProgressItemDto
import ir.aispeaking.network.model.stage.dto.SyncProgressRequestDto
import kotlin.time.Clock
import org.koin.core.annotation.Single

@Single
class StageProgressRepositoryImpl(
    private val localGuestDataSource: LocalGuestProgressDataSource,
    private val progressApi: StageProgressApi
) : StageProgressRepository {

    override suspend fun saveLocalGuestProgress(stageId: String, stars: Int, score: Int): DataResult<Unit> {
        val now = Clock.System.now().toString()
        localGuestDataSource.saveProgress(
            LocalGuestProgress(
                stageId = stageId,
                stars = stars,
                score = score,
                completedAt = now
            )
        )
        return DataResult.Success(Unit)
    }

    override suspend fun getLocalGuestProgress(): DataResult<Map<String, StageProgress>> {
        val map = localGuestDataSource.getProgressList().associate { item ->
            item.stageId to StageProgress(
                id = item.stageId,
                userId = "guest",
                stageId = item.stageId,
                stars = item.stars,
                bestScore = item.score,
                repeatCount = 1,
                completedAt = item.completedAt,
                updatedAt = item.completedAt
            )
        }
        return DataResult.Success(map)
    }

    override suspend fun syncGuestProgress(): DataResult<List<StageProgress>> {
        val localList = localGuestDataSource.getProgressList()
        if (localList.isEmpty()) {
            return DataResult.Success(emptyList())
        }

        val request = SyncProgressRequestDto(
            items = localList.map {
                SyncProgressItemDto(
                    stageId = it.stageId,
                    stars = it.stars,
                    bestScore = it.score,
                    completedAt = it.completedAt
                )
            }
        )

        val networkResult = safeCall { progressApi.syncProgress(request) }
        if (networkResult !is DataResult.Success) {
            return DataResult.Failure((networkResult as DataResult.Failure).appError)
        }

        localGuestDataSource.clear()
        val domainItems = networkResult.data.syncedItems.map {
            StageProgress(
                id = it.stageId,
                userId = "",
                stageId = it.stageId,
                stars = it.stars,
                bestScore = it.bestScore,
                repeatCount = it.repeatCount,
                completedAt = it.completedAt,
                updatedAt = it.updatedAt
            )
        }
        return DataResult.Success(domainItems)
    }
}
