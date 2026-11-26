package ir.speaking.feature.dictionary

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import ir.speaking.core.extension.getClient
import ir.speaking.feature.dictionary.db.LexiconTable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DictionaryRoutingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testDictionaryLookupEndpoint() = testApplication {
        val client = getClient(dropTables = true)

        // 1. Invalid empty word -> 400 Bad Request
        val badRes = client.post("/api/v2/dictionary/lookup") {
            contentType(ContentType.Application.Json)
            setBody("""{"word": ""}""")
        }
        assertEquals(HttpStatusCode.BadRequest, badRes.status)

        // 1b. Deterministic invalid inputs -> 400 Bad Request with errorMessage
        listOf("12345", "سلام", "aaaaaaa").forEach { invalidWord ->
            val invalidRes = client.post("/api/v2/dictionary/lookup") {
                contentType(ContentType.Application.Json)
                setBody("""{"word": "$invalidWord"}""")
            }
            assertEquals(HttpStatusCode.BadRequest, invalidRes.status, "Expected 400 for '$invalidWord'")
            val bodyJson = json.parseToJsonElement(invalidRes.bodyAsText()).jsonObject
            assertTrue(bodyJson["errorMessage"]?.jsonPrimitive?.content?.isNotBlank() == true)
        }

        // 2. Pre-seed a word in Lexicon and verify lookup returns 200 OK
        transaction {
            LexiconTable.insert {
                it[normalizedText] = "hello"
                it[headword] = "hello"
                it[phonetic] = "/həˈloʊ/"
                it[partOfSpeech] = "interjection"
                it[contextMeaningFa] = "سلام"
                it[audioUrl] = "/audio_cache/words/hello.mp3"
            }
        }

        val preseedRes = client.post("/api/dictionary/lookup") {
            contentType(ContentType.Application.Json)
            setBody("""{"word": "hello", "contextSentence": "Hello, how are you?"}""")
        }
        assertEquals(HttpStatusCode.OK, preseedRes.status)
    }
}
