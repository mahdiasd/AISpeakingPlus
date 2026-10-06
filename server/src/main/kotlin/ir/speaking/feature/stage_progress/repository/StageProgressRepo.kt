package ir.speaking.feature.stage_progress.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.stage_progress.dto.SyncProgressItem
import ir.speaking.feature.stage_progress.dto.SyncProgressItemResult
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.*

data class ProgressSaveResult(
    val stars: Int,
    val score: Int,
    val isHighScore: Boolean,
    val repeatCount: Int
)

@Single
class StageProgressRepo {

    suspend fun syncProgress(
        userId: UUID,
        items: List<SyncProgressItem>
    ): List<SyncProgressItemResult> = suspendTransaction {
        val now = Clock.System.now()
        val results = mutableListOf<SyncProgressItemResult>()

        for (item in items) {
            val existingRow = StageProgressTable.selectAll()
                .where {
                    (StageProgressTable.userId eq userId) and
                    (StageProgressTable.stageId eq item.stageId)
                }
                .firstOrNull()

            if (existingRow != null) {
                val existingStars = existingRow[StageProgressTable.stars]
                val existingScore = existingRow[StageProgressTable.bestScore]
                val existingRepeat = existingRow[StageProgressTable.repeatCount]
                val existingCompletedAt = existingRow[StageProgressTable.completedAt]

                val finalStars = maxOf(existingStars, item.stars)
                val finalScore = maxOf(existingScore, item.bestScore)

                StageProgressTable.update({
                    (StageProgressTable.userId eq userId) and
                    (StageProgressTable.stageId eq item.stageId)
                }) {
                    it[StageProgressTable.stars] = finalStars
                    it[StageProgressTable.bestScore] = finalScore
                    it[StageProgressTable.updatedAt] = now
                }

                results.add(
                    SyncProgressItemResult(
                        stageId = item.stageId,
                        stars = finalStars,
                        bestScore = finalScore,
                        repeatCount = existingRepeat,
                        completedAt = existingCompletedAt.toString(),
                        updatedAt = now.toString()
                    )
                )
            } else {
                StageProgressTable.insert {
                    it[StageProgressTable.userId] = userId
                    it[StageProgressTable.stageId] = item.stageId
                    it[StageProgressTable.stars] = item.stars
                    it[StageProgressTable.bestScore] = item.bestScore
                    it[StageProgressTable.repeatCount] = 1
                    it[StageProgressTable.completedAt] = now
                    it[StageProgressTable.updatedAt] = now
                }

                results.add(
                    SyncProgressItemResult(
                        stageId = item.stageId,
                        stars = item.stars,
                        bestScore = item.bestScore,
                        repeatCount = 1,
                        completedAt = now.toString(),
                        updatedAt = now.toString()
                    )
                )
            }
        }
        results
    }

    suspend fun saveOrUpdateProgress(
        userId: UUID,
        stageId: String,
        stars: Int,
        score: Int
    ): ProgressSaveResult = suspendTransaction {
        val existingRow = StageProgressTable.selectAll()
            .where {
                (StageProgressTable.userId eq userId) and
                (StageProgressTable.stageId eq stageId)
            }
            .firstOrNull()

        val now = Clock.System.now()

        if (existingRow != null) {
            val existingStars = existingRow[StageProgressTable.stars]
            val existingScore = existingRow[StageProgressTable.bestScore]
            val existingRepeat = existingRow[StageProgressTable.repeatCount]

            val isHighScore = stars > existingStars || (stars == existingStars && score > existingScore)
            val newStars = maxOf(existingStars, stars)
            val newScore = maxOf(existingScore, score)
            val newRepeat = existingRepeat + 1

            StageProgressTable.update({
                (StageProgressTable.userId eq userId) and
                (StageProgressTable.stageId eq stageId)
            }) {
                it[StageProgressTable.stars] = newStars
                it[StageProgressTable.bestScore] = newScore
                it[StageProgressTable.repeatCount] = newRepeat
                it[StageProgressTable.updatedAt] = now
            }

            ProgressSaveResult(newStars, newScore, isHighScore, newRepeat)
        } else {
            StageProgressTable.insert {
                it[StageProgressTable.userId] = userId
                it[StageProgressTable.stageId] = stageId
                it[StageProgressTable.stars] = stars
                it[StageProgressTable.bestScore] = score
                it[StageProgressTable.repeatCount] = 1
                it[StageProgressTable.completedAt] = now
                it[StageProgressTable.updatedAt] = now
            }

            ProgressSaveResult(stars, score, isHighScore = true, repeatCount = 1)
        }
    }
}
