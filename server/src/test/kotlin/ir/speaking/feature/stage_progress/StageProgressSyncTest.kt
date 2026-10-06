package ir.speaking.feature.stage_progress

import ir.speaking.feature.stage_progress.dto.SyncProgressItem
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.runBlocking

class StageProgressSyncTest {

    private lateinit var db: Database
    private val repo = StageProgressRepo()

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_sync_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            SchemaUtils.create(UserTable, StageTable, StageProgressTable)
        }
    }

    @AfterEach
    fun teardown() {
        transaction(db) {
            SchemaUtils.drop(StageProgressTable, StageTable, UserTable)
        }
    }

    @Test
    fun `test conflict resolution takes max of stars and score`() = runBlocking {
        val userId = UUID.randomUUID()
        transaction(db) {
            UserTable.insert {
                it[id] = userId
                it[mobile] = "+989123456789"
            }
            StageTable.insert {
                it[id] = "stage-01"
                it[orderIndex] = 1
                it[title] = "Stage 1"
                it[titleFa] = "مرحله ۱"
                it[briefing] = "Briefing"
                it[briefingFa] = "توضیحات"
                it[targetObjective] = "Objective"
                it[targetObjectiveFa] = "هدف مرحله"
                it[characterBehavior] = "رفتار کاراکتر"
                it[backgroundUrl] = "https://example.com/bg.png"
                it[characterName] = "Agent"
            }
        }

        // 1. Initial save: cloud has 1 star, score 60
        repo.saveOrUpdateProgress(userId, "stage-01", stars = 1, score = 60)

        // 2. Sync incoming: local has 2 stars, score 85 -> should update to 2 stars, 85 score
        val syncResult1 = repo.syncProgress(
            userId = userId,
            items = listOf(
                SyncProgressItem(
                    stageId = "stage-01",
                    stars = 2,
                    bestScore = 85,
                    completedAt = "2026-09-19T12:00:00Z"
                )
            )
        )

        assertEquals(1, syncResult1.size)
        assertEquals(2, syncResult1[0].stars)
        assertEquals(85, syncResult1[0].bestScore)

        // 3. Sync incoming: local has 1 star, score 50 (lower) -> should keep 2 stars, 85 score
        val syncResult2 = repo.syncProgress(
            userId = userId,
            items = listOf(
                SyncProgressItem(
                    stageId = "stage-01",
                    stars = 1,
                    bestScore = 50,
                    completedAt = "2026-09-19T12:00:00Z"
                )
            )
        )

        assertEquals(1, syncResult2.size)
        assertEquals(2, syncResult2[0].stars)
        assertEquals(85, syncResult2[0].bestScore)

        // 4. Sync incoming: local has 2 stars, score 95 (higher score) -> should update score to 95
        val syncResult3 = repo.syncProgress(
            userId = userId,
            items = listOf(
                SyncProgressItem(
                    stageId = "stage-01",
                    stars = 2,
                    bestScore = 95,
                    completedAt = "2026-09-19T12:00:00Z"
                )
            )
        )

        assertEquals(2, syncResult3[0].stars)
        assertEquals(95, syncResult3[0].bestScore)
    }
}
