package ir.speaking.feature.dictionary

import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.dictionary.repository.LexiconRepository
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LexiconRepositoryTest {

    private lateinit var db: Database
    private val repository = LexiconRepository()

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_lexicon_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
            driver = "org.h2.Driver"
        )
        transaction(db) {
            SchemaUtils.create(LexiconTable)
        }
    }

    @AfterEach
    fun teardown() {
        transaction(db) {
            SchemaUtils.drop(LexiconTable)
        }
    }

    @Test
    fun testSaveAndFindByNormalizedText() = runBlocking {
        val saved = repository.save(
            normalizedText = "withdraw",
            headword = "withdraw",
            phonetic = "/wɪðˈdrɔː/",
            partOfSpeech = "verb",
            contextMeaningFa = "برداشت کردن (پول)",
            otherMeaningsJson = "[\"عقب‌نشینی کردن\"]",
            definitionEn = "to take money out of a bank account",
            sampleSentenceEn = "I need to withdraw cash.",
            sampleSentenceFa = "باید پول نقد برداشت کنم.",
            audioUrl = "/audio_cache/words/withdraw.mp3"
        )

        assertNotNull(saved.id)
        assertEquals("withdraw", saved.normalizedText)
        assertEquals("/wɪðˈdrɔː/", saved.phonetic)

        val found = repository.findByNormalizedText("WithDraw ")
        assertNotNull(found)
        assertEquals(saved.id, found.id)
        assertEquals("برداشت کردن (پول)", found.contextMeaningFa)

        val notFound = repository.findByNormalizedText("nonexistent")
        assertNull(notFound)
    }

    @Test
    fun testUpdateAudioUrl() = runBlocking {
        val saved = repository.save(
            normalizedText = "deposit",
            headword = "deposit",
            phonetic = "/dɪˈpɒz.ɪt/",
            partOfSpeech = "verb",
            contextMeaningFa = "واریز کردن",
            otherMeaningsJson = null,
            definitionEn = null,
            sampleSentenceEn = null,
            sampleSentenceFa = null,
            audioUrl = null
        )

        val updated = repository.updateAudioUrl(UUID.fromString(saved.id), "/audio/deposit.mp3")
        assertTrue(updated)

        val reloaded = repository.findById(UUID.fromString(saved.id))
        assertNotNull(reloaded)
        assertEquals("/audio/deposit.mp3", reloaded.audioUrl)
    }
}
