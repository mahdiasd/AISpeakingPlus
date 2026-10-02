package ir.speaking.feature.stage_progress

import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReplayHighScoreTest {

    private lateinit var db: Database
    private val repo = StageProgressRepo()

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_replay_${UUID.randomUUID()};DB_CLOSE_DELAY=-1",
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
    fun `test replay preserves highest star rating and score while incrementing repeat count`() = runBlocking {
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

        // 1. Initial attempt: 3 stars, 100 score
        val attempt1 = repo.saveOrUpdateProgress(userId, "stage-01", stars = 3, score = 100)
        assertEquals(3, attempt1.stars)
        assertEquals(100, attempt1.score)
        assertEquals(1, attempt1.repeatCount)
        assertTrue(attempt1.isHighScore)

        // 2. Replay with worse performance: 1 star, 40 score
        val attempt2 = repo.saveOrUpdateProgress(userId, "stage-01", stars = 1, score = 40)
        assertEquals(3, attempt2.stars) // Preserved
        assertEquals(100, attempt2.score) // Preserved
        assertEquals(2, attempt2.repeatCount) // Incremented
        assertFalse(attempt2.isHighScore)

        // 3. Replay with better score: 3 stars, 120 score
        val attempt3 = repo.saveOrUpdateProgress(userId, "stage-01", stars = 3, score = 120)
        assertEquals(3, attempt3.stars)
        assertEquals(120, attempt3.score) // Improved
        assertEquals(3, attempt3.repeatCount) // Incremented
        assertTrue(attempt3.isHighScore)
    }
}
