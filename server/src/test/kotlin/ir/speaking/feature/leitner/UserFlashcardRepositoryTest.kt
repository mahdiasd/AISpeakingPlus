package ir.speaking.feature.leitner

import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.dictionary.repository.LexiconRepository
import ir.speaking.feature.leitner.db.UserFlashcardTable
import ir.speaking.feature.leitner.repository.UserFlashcardRepository
import ir.speaking.feature.user.db.UserTable
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserFlashcardRepositoryTest {

    private lateinit var db: Database
    private val flashcardRepo = UserFlashcardRepository()
    private val lexiconRepo = LexiconRepository()

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_leitner_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            SchemaUtils.create(UserTable, LexiconTable, UserFlashcardTable)
        }
    }

    @AfterEach
    fun teardown() {
        transaction(db) {
            SchemaUtils.drop(UserFlashcardTable, LexiconTable, UserTable)
        }
    }

    private fun createTestUser(): UUID {
        return transaction(db) {
            UserTable.insertAndGetId {
                it[mobile] = "0912" + (1000000..9999999).random()
                it[nickName] = "Tester"
            }.value
        }
    }

    @Test
    fun testAddAndRetrieveFlashcard() = runBlocking {
        val userId = createTestUser()
        val lexicon = lexiconRepo.save(
            normalizedText = "serendipity",
            headword = "serendipity",
            phonetic = "/ˌser.ənˈdɪp.ə.ti/",
            partOfSpeech = "noun",
            contextMeaningFa = "خوش‌اقبالی غیرمنتظره",
            otherMeaningsJson = null,
            definitionEn = "finding valuable things unexpectedly",
            sampleSentenceEn = "It was pure serendipity.",
            sampleSentenceFa = "کاملاً از سر خوش‌اقبالی بود.",
            audioUrl = "/audio/serendipity.mp3"
        )

        val card = flashcardRepo.addCard(
            userId = userId,
            lexiconId = UUID.fromString(lexicon.id)
        )

        assertNotNull(card.id)
        assertEquals(1, card.boxNumber)
        assertEquals("serendipity", card.headword)
        assertEquals("خوش‌اقبالی غیرمنتظره", card.meaningFa)
        assertEquals("/audio/serendipity.mp3", card.audioUrl)

        // Test box counts
        val counts = flashcardRepo.getBoxCounts(userId)
        assertEquals(1L, counts[1])
        assertEquals(0L, counts[2])

        // Test due cards
        val due = flashcardRepo.getDueCards(userId, Clock.System.now())
        assertEquals(1, due.size)
        assertEquals(card.id, due.first().id)
    }

    @Test
    fun testReviewPromotionAndStepDownPenalty() = runBlocking {
        val userId = createTestUser()
        val card = flashcardRepo.addCard(
            userId = userId,
            lexiconId = null,
            customWord = "resilient",
            customMeaning = "تاب‌آور و انعطاف‌پذیر"
        )

        val cardId = UUID.fromString(card.id)
        val now = Clock.System.now()

        // 1. Remembered in Box 1 -> Advances to Box 2
        val afterPromote = flashcardRepo.updateReviewResult(cardId, userId, remembered = true, now = now)
        assertNotNull(afterPromote)
        assertEquals(2, afterPromote.boxNumber)
        assertEquals(1, afterPromote.reviewCount)

        // 2. Remembered in Box 2 -> Advances to Box 3
        val afterPromote2 = flashcardRepo.updateReviewResult(cardId, userId, remembered = true, now = now)
        assertNotNull(afterPromote2)
        assertEquals(3, afterPromote2.boxNumber)
        assertEquals(2, afterPromote2.reviewCount)

        // 3. Forgotten in Box 3 -> Drops 1 step to Box 2 (friendly penalty, not reset to Box 1)
        val afterDemote = flashcardRepo.updateReviewResult(cardId, userId, remembered = false, now = now)
        assertNotNull(afterDemote)
        assertEquals(2, afterDemote.boxNumber)
        assertEquals(3, afterDemote.reviewCount)

        // 4. Forgotten in Box 2 -> Drops 1 step to Box 1
        val afterDemote2 = flashcardRepo.updateReviewResult(cardId, userId, remembered = false, now = now)
        assertNotNull(afterDemote2)
        assertEquals(1, afterDemote2.boxNumber)

        // 5. Forgotten in Box 1 -> Stays in Box 1
        val afterDemoteInBox1 = flashcardRepo.updateReviewResult(cardId, userId, remembered = false, now = now)
        assertNotNull(afterDemoteInBox1)
        assertEquals(1, afterDemoteInBox1.boxNumber)
    }

    @Test
    fun testDeleteCard() = runBlocking {
        val userId = createTestUser()
        val card = flashcardRepo.addCard(
            userId = userId,
            lexiconId = null,
            customWord = "temporary",
            customMeaning = "موقت"
        )

        val deleted = flashcardRepo.deleteCard(UUID.fromString(card.id), userId)
        assertTrue(deleted)

        val all = flashcardRepo.getAllCards(userId)
        assertTrue(all.isEmpty())
    }
}
