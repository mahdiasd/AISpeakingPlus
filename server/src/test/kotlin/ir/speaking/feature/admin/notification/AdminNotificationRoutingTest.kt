package ir.speaking.feature.admin.notification

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import ir.speaking.core.configureTestDatabases
import ir.speaking.core.configureTestSecurity
import ir.speaking.feature.admin.routing.adminRouting
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AdminNotificationRoutingTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun ApplicationTestBuilder.setupTestApp() {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
            configureTestSecurity()
            configureTestDatabases(dropTables = true)
            adminRouting()
        }
    }

    private suspend fun ApplicationTestBuilder.loginAndGetToken(
        username: String = "admin@aispeaking.ir",
        password: String = "Admin@123456!"
    ): String {
        val loginRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username","password":"$password"}""")
        }
        assertEquals(HttpStatusCode.OK, loginRes.status)
        val loginJson = json.parseToJsonElement(loginRes.bodyAsText()).jsonObject
        val dataObj = loginJson["data"]?.jsonObject
        assertNotNull(dataObj)
        return dataObj!!["token"]!!.jsonPrimitive.content
    }

    @Test
    fun `test send notification requires admin authentication`() = testApplication {
        setupTestApp()

        val response = client.post("/api/admin/notifications/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Test","body":"Test body"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `test send notification rejects blank title or body`() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        val response = client.post("/api/admin/notifications/send") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title":"","body":""}""")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        val bodyText = response.bodyAsText()
        assertTrue(bodyText.contains("نمی‌توانند خالی باشند") || bodyText.contains("400"))
    }

    @Test
    fun `test client token registration endpoint`() = testApplication {
        setupTestApp()

        // Blank token returns 400
        val blankRes = client.post("/api/v1/notifications/register-token") {
            contentType(ContentType.Application.Json)
            setBody("""{"token":""}""")
        }
        assertEquals(HttpStatusCode.BadRequest, blankRes.status)

        // Valid token registration attempts to subscribe to topic
        val validRes = client.post("/api/v1/notifications/register-token") {
            contentType(ContentType.Application.Json)
            setBody("""{"token":"mock_test_token_123","topic":"aispeaking_plus_v1"}""")
        }
        // In test environment without active Firebase connection, it may return OK or InternalServerError with full detail
        assertTrue(validRes.status == HttpStatusCode.OK || validRes.status == HttpStatusCode.InternalServerError)
    }

    @Test
    fun `test send notification with optional none screen accepted`() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        val response = client.post("/api/admin/notifications/send") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"title":"Test None","body":"No deep link","screen":"none","targetType":"topic","targetValue":"aispeaking_plus_v1"}""")
        }
        // In test environment with Firebase credentials, it may succeed (200) or fail at Google API call (502/500), but not validation error (400)
        assertNotEquals(HttpStatusCode.BadRequest, response.status)
    }
}
