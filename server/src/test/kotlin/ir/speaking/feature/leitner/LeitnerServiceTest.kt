package ir.speaking.feature.leitner

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import ir.speaking.core.network.api.AiApiService
import ir.speaking.core.network.di.AiClientManager
import ir.speaking.core.redis.di.RedisClientHolder
import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.dictionary.repository.LexiconRepository
import ir.speaking.feature.dictionary.service.DictionaryService
import ir.speaking.feature.dictionary.validation.InvalidWordException
import ir.speaking.feature.leitner.db.UserFlashcardTable
import ir.speaking.feature.leitner.db.UserLeitnerSettingsTable
import ir.speaking.feature.leitner.dto.AddFlashcardRequest
import ir.speaking.feature.leitner.repository.UserFlashcardRepository
import ir.speaking.feature.leitner.service.LeitnerService
import ir.speaking.feature.tts.service.TtsConfig
import ir.speaking.feature.tts.service.TtsService
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LeitnerServiceTest {

    private lateinit var db: Database
    private val flashcardRepo = UserFlashcardRepository()
    private val lexiconRepo = LexiconRepository()
    private val redisHolder = RedisClientHolder(null)
    private lateinit var leitnerService: LeitnerService

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_leitner_svc_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            SchemaUtils.create(UserTable, LexiconTable, UserFlashcardTable, UserLeitnerSettingsTable)
        }
        val fakeAi = object : AiApiService(AiClientManager()) {
            override suspend fun requestRaw(messages: List<ChatMessage>, responseFormat: ChatResponseFormat): Result<String> {
                return Result.success("""{"isValid": true, "headword": "word", "contextMeaningFa": "معنی"}""")
            }
        }
        val ttsService = TtsService(
            TtsConfig(false, "models/none", "", false, 1, 0, 1.0f, "build/test_audio_cache_leitner")
        )
        val dictService = DictionaryService(lexiconRepo, fakeAi, ttsService, redisHolder)
        leitnerService = LeitnerService(flashcardRepo, redisHolder, lexiconRepo, dictService)
    }

    @AfterEach
    fun teardown() {
        transaction(db) {
            SchemaUtils.drop(UserLeitnerSettingsTable, UserFlashcardTable, LexiconTable, UserTable)
        }
    }

    private fun createTestUser(): UUID {
        return transaction(db) {
            UserTable.insertAndGetId {
                it[mobile] = "0912" + (1000000..9999999).random()
                it[nickName] = "LeitnerTester"
            }.value
        }
    }

    @Test
    fun testCardAdditionAndStats() = runBlocking {
        val userId = createTestUser()

        val card1 = leitnerService.addCard(
            userId,
            AddFlashcardRequest(
                customWord = "ubiquitous",
                customMeaning = "همه‌جا حاضر و فراگیر"
            )
        )
        assertNotNull(card1.id)
        assertEquals(1, card1.boxNumber)

        val stats = leitnerService.getStats(userId)
        assertEquals(1L, stats.totalCards)
        assertEquals(1L, stats.dueCards)
        assertEquals(1L, stats.boxCounts[1])

        val dueCards = leitnerService.getCards(userId, dueOnly = true)
        assertEquals(1, dueCards.size)
        assertEquals(card1.id, dueCards.first().id)
    }

    @Test
    fun testReviewCycleAndStepDownPenalty() = runBlocking {
        val userId = createTestUser()
        val card = leitnerService.addCard(
            userId,
            AddFlashcardRequest(
                customWord = "ephemeral",
                customMeaning = "زودگذر و ناپایدار"
            )
        )
        val cardId = UUID.fromString(card.id)

        // Success: Box 1 -> Box 2
        val b2 = leitnerService.reviewCard(userId, cardId, remembered = true)
        assertNotNull(b2)
        assertEquals(2, b2.boxNumber)

        // Success: Box 2 -> Box 3
        val b3 = leitnerService.reviewCard(userId, cardId, remembered = true)
        assertNotNull(b3)
        assertEquals(3, b3.boxNumber)

        // Failure: Box 3 -> Box 2 (1-step penalty, NOT reset to Box 1)
        val penalty = leitnerService.reviewCard(userId, cardId, remembered = false)
        assertNotNull(penalty)
        assertEquals(2, penalty.boxNumber)
    }

    @Test
    fun testReminderTimeConfiguration() = runBlocking {
        val userId = createTestUser()

        // Default should be 20:00
        val defaultReminder = leitnerService.getReminderTime(userId)
        assertEquals("20:00", defaultReminder.reminderTime)
        assertTrue(defaultReminder.enabled)

        // Set to 08:30
        val updated = leitnerService.setReminderTime(userId, "08:30", enabled = true)
        assertEquals("08:30", updated.reminderTime)
        assertTrue(updated.enabled)

        // Retrieve again
        val retrieved = leitnerService.getReminderTime(userId)
        assertEquals("08:30", retrieved.reminderTime)
        assertTrue(retrieved.enabled)
    }

    @Test
    fun testDeleteCardRemovesFromDeckAndUpdatesStats() = runBlocking {
        val userId = createTestUser()
        val card = leitnerService.addCard(
            userId,
            AddFlashcardRequest(customWord = "resilient", customMeaning = "تاب‌آور")
        )
        assertEquals(1L, leitnerService.getStats(userId).totalCards)

        val deleted = leitnerService.deleteCard(userId, UUID.fromString(card.id))
        assertTrue(deleted)
        assertEquals(0L, leitnerService.getStats(userId).totalCards)
        assertTrue(leitnerService.getCards(userId).isEmpty())

        // Deleting non-existent card returns false
        assertFalse(leitnerService.deleteCard(userId, UUID.fromString(card.id)))
    }

    @Test
    fun testAddCardRejectsInvalidWordsAndUnknownLexicon() = runBlocking {
        val userId = createTestUser()

        assertFailsWith<InvalidWordException> {
            leitnerService.addCard(userId, AddFlashcardRequest(customWord = "12345", customMeaning = "test"))
        }
        assertFailsWith<InvalidWordException> {
            leitnerService.addCard(userId, AddFlashcardRequest(customWord = "سلام", customMeaning = "test"))
        }
        assertFailsWith<InvalidWordException> {
            leitnerService.addCard(userId, AddFlashcardRequest(customWord = "aaaaaaa", customMeaning = "test"))
        }
        assertFailsWith<InvalidWordException> {
            leitnerService.addCard(userId, AddFlashcardRequest(lexiconId = UUID.randomUUID().toString()))
        }

        val fakeAi = object : AiApiService(AiClientManager()) {
            override suspend fun requestRaw(messages: List<ChatMessage>, responseFormat: ChatResponseFormat): Result<String> {
                return Result.success("""{"isValid": false, "rejectionReasonFa": "عبارت نامعتبر است"}""")
            }
        }
        val ttsService = TtsService(
            TtsConfig(false, "models/none", "", false, 1, 0, 1.0f, "build/test_audio_cache_leitner")
        )
        val dictService = DictionaryService(lexiconRepo, fakeAi, ttsService, redisHolder)
        val guardedLeitner = LeitnerService(flashcardRepo, redisHolder, lexiconRepo, dictService)

        assertFailsWith<InvalidWordException> {
            guardedLeitner.addCard(userId, AddFlashcardRequest(customWord = "abbas bo azar", customMeaning = "test"))
        }
    }
}
