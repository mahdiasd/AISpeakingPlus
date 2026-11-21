package ir.speaking.feature.leitner

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import ir.speaking.core.extension.getClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LeitnerRoutingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun testLeitnerFullLifecycle() = testApplication {
        val client = getClient(dropTables = true)

        // 1. Unauthenticated access -> 401 Unauthorized
        val unauthRes = client.get("/api/v2/leitner/cards")
        assertEquals(HttpStatusCode.Unauthorized, unauthRes.status)

        // 2. Obtain test token using bypass credentials
        val otpRes = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "09152413498", "otpCode": "87799"}""")
        }
        assertEquals(HttpStatusCode.OK, otpRes.status)
        val otpBody = json.parseToJsonElement(otpRes.bodyAsText()).jsonObject
        val token = otpBody["data"]?.jsonObject?.get("token")?.jsonPrimitive?.content
        assertNotNull(token)

        val authHeader = "Bearer $token"

        // 3. Initial empty cards list -> 200 OK
        val cardsRes = client.get("/api/v2/leitner/cards") {
            header(HttpHeaders.Authorization, authHeader)
        }
        assertEquals(HttpStatusCode.OK, cardsRes.status)

        // 4. Add a custom flashcard -> 200 OK
        val addRes = client.post("/api/v2/leitner/cards") {
            header(HttpHeaders.Authorization, authHeader)
            contentType(ContentType.Application.Json)
            setBody("""
                {
                    "customWord": "perseverance",
                    "customMeaning": "پشتکار و استقامت",
                    "customExample": "Success requires hard work and perseverance."
                }
            """.trimIndent())
        }
        assertEquals(HttpStatusCode.OK, addRes.status)
        val addBody = json.parseToJsonElement(addRes.bodyAsText()).jsonObject
        val cardData = addBody["data"]?.jsonObject
        assertNotNull(cardData)
        val cardId = cardData["id"]?.jsonPrimitive?.content
        assertNotNull(cardId)
        assertEquals("1", cardData["boxNumber"]?.jsonPrimitive?.content)

        // 5. Query stats -> 200 OK
        val statsRes = client.get("/api/v2/leitner/stats") {
            header(HttpHeaders.Authorization, authHeader)
        }
        assertEquals(HttpStatusCode.OK, statsRes.status)

        // 6. Review card (Remembered = true) -> Advances to Box 2
        val reviewRes = client.post("/api/v2/leitner/review") {
            header(HttpHeaders.Authorization, authHeader)
            contentType(ContentType.Application.Json)
            setBody("""{"cardId": "$cardId", "remembered": true}""")
        }
        assertEquals(HttpStatusCode.OK, reviewRes.status)
        val reviewBody = json.parseToJsonElement(reviewRes.bodyAsText()).jsonObject
        val updatedCard = reviewBody["data"]?.jsonObject
        assertNotNull(updatedCard)
        assertEquals("2", updatedCard["boxNumber"]?.jsonPrimitive?.content)

        // 7. Update reminder time -> 200 OK
        val reminderPostRes = client.post("/api/v2/leitner/reminder-time") {
            header(HttpHeaders.Authorization, authHeader)
            contentType(ContentType.Application.Json)
            setBody("""{"hour": 21, "minute": 15, "enabled": true}""")
        }
        assertEquals(HttpStatusCode.OK, reminderPostRes.status)
        val reminderBody = json.parseToJsonElement(reminderPostRes.bodyAsText()).jsonObject
        val reminderData = reminderBody["data"]?.jsonObject
        assertNotNull(reminderData)
        assertEquals("21:15", reminderData["reminderTime"]?.jsonPrimitive?.content)

        // 8. Get reminder time -> 200 OK
        val reminderGetRes = client.get("/api/v2/leitner/reminder-time") {
            header(HttpHeaders.Authorization, authHeader)
        }
        assertEquals(HttpStatusCode.OK, reminderGetRes.status)
        val getReminderData = json.parseToJsonElement(reminderGetRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(getReminderData)
        assertEquals("21:15", getReminderData["reminderTime"]?.jsonPrimitive?.content)

        // 9. Legacy /api/leitner endpoints also work cleanly
        val legacyCards = client.get("/api/leitner/cards?dueOnly=true") {
            header(HttpHeaders.Authorization, authHeader)
        }
        assertEquals(HttpStatusCode.OK, legacyCards.status)
    }
}
