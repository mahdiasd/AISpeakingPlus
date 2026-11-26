package ir.speaking.feature.dictionary

import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
import ir.speaking.core.network.api.AiApiService
import ir.speaking.core.network.di.AiClientManager
import ir.speaking.core.redis.di.RedisClientHolder
import ir.speaking.feature.dictionary.db.LexiconTable
import ir.speaking.feature.dictionary.repository.LexiconRepository
import ir.speaking.feature.dictionary.service.DictionaryService
import ir.speaking.feature.dictionary.validation.InvalidWordException
import ir.speaking.feature.tts.service.TtsConfig
import ir.speaking.feature.tts.service.TtsService
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DictionaryServiceTest {

    private lateinit var db: Database
    private val lexiconRepo = LexiconRepository()
    private val redisHolder = RedisClientHolder(null) // in-memory fallback
    private val ttsConfig = TtsConfig(
        enabled = false,
        modelDir = "models/none",
        modelUrl = "",
        autoDownload = false,
        numThreads = 1,
        defaultSid = 0,
        defaultSpeed = 1.0f,
        cacheDir = "build/test_audio_cache"
    )
    private val ttsService = TtsService(ttsConfig)

    private class FakeAiApiService(
        private val cannedJson: String
    ) : AiApiService(AiClientManager()) {
        override suspend fun requestRaw(
            messages: List<ChatMessage>,
            responseFormat: ChatResponseFormat
        ): Result<String> {
            return Result.success(cannedJson)
        }
    }

    @BeforeEach
    fun setup() {
        db = Database.connect(
            url = "jdbc:h2:mem:test_dict_service_${UUID.randomUUID()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
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
        File("build/test_audio_cache").deleteRecursively()
    }

    @Test
    fun testLookupNewWordWithStructuredAiJson() = runBlocking {
        val sampleAiJson = """
            ```json
            {
              "headword": "withdraw",
              "phonetic": "/wɪðˈdrɔː/",
              "partOfSpeech": "verb",
              "contextMeaningFa": "برداشت کردن (پول)",
              "definitionEn": "to take money out of an account",
              "otherMeaningsFa": ["عقب‌نشینی کردن", "صرف‌نظر کردن"],
              "sampleSentenceEn": "I need to withdraw some cash from the ATM.",
              "sampleSentenceFa": "باید مقداری پول نقد از دستگاه خودپرداز برداشت کنم."
            }
            ```
        """.trimIndent()

        val fakeAiService = FakeAiApiService(sampleAiJson)
        val service = DictionaryService(lexiconRepo, fakeAiService, ttsService, redisHolder)

        val result = service.lookup("WithDraw! ", "I need to withdraw cash.")
        assertNotNull(result)
        assertEquals("withdraw", result.headword)
        assertEquals("/wɪðˈdrɔː/", result.phonetic)
        assertEquals("verb", result.partOfSpeech)
        assertEquals("برداشت کردن (پول)", result.contextMeaningFa)
        assertEquals(2, result.otherMeaningsFa.size)
        assertEquals("عقب‌نشینی کردن", result.otherMeaningsFa[0])
        assertEquals("to take money out of an account", result.definitionEn)

        // Verify persisted to DB
        val inDb = lexiconRepo.findByNormalizedText("withdraw")
        assertNotNull(inDb)
        assertEquals(result.id, inDb.id)

        // Second lookup should be served from cache without AI call
        val hitResult = service.lookup("withdraw")
        assertEquals(result.id, hitResult.id)
        assertEquals("برداشت کردن (پول)", hitResult.contextMeaningFa)
    }

    @Test
    fun testWordNormalization() {
        val service = DictionaryService(lexiconRepo, FakeAiApiService("{}"), ttsService, redisHolder)
        assertEquals("hello", service.normalize("  Hello!  "))
        assertEquals("state-of-the-art", service.normalize("State-of-the-art?"))
        assertEquals("don't", service.normalize("  Don't... "))
    }

    @Test
    fun testLookupRejectsAiInvalidWordWithoutPersisting() = runBlocking {
        val rejectedJson = """
            {
              "isValid": false,
              "rejectionReasonFa": "عبارت واردشده یک کلمه یا اصطلاح معتبر انگلیسی نیست."
            }
        """.trimIndent()

        val service = DictionaryService(lexiconRepo, FakeAiApiService(rejectedJson), ttsService, redisHolder)
        val ex = assertFailsWith<InvalidWordException> {
            service.lookup("abbas bo azar")
        }
        assertTrue(ex.message?.contains("معتبر") == true)
        assertNull(lexiconRepo.findByNormalizedText("abbas bo azar"))
    }

    @Test
    fun testLookupRejectsDeterministicInvalidInputs() = runBlocking {
        val service = DictionaryService(lexiconRepo, FakeAiApiService("{}"), ttsService, redisHolder)

        assertFailsWith<InvalidWordException> { service.lookup("سلام") }
        assertFailsWith<InvalidWordException> { service.lookup("12345") }
        assertFailsWith<InvalidWordException> { service.lookup("aaaaaaa") }
        assertFailsWith<InvalidWordException> { service.lookup("bcdfghj") }
    }
}
