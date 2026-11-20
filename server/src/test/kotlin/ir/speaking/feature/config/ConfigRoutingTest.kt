package ir.speaking.feature.config

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import ir.speaking.core.testModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigRoutingTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun testV1ConfigPostReturnsForceUpdate() = testApplication {
        application {
            testModule(dropTables = false)
        }

        val response = client.post("/api/v1/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"deviceName":"Pixel 7","androidVersion":"14","firebaseToken":"sample_fcm_token_123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val parsed = json.parseToJsonElement(body).jsonObject
        val data = parsed["data"]?.jsonObject
        assertNotNull(data, "Expected data object in response")

        val update = data["update"]?.jsonObject
        assertNotNull(update, "Expected update object in data")

        val forceVersion = update["force_version"]?.jsonPrimitive?.content?.toIntOrNull()
        assertTrue((forceVersion ?: 0) >= 10, "force_version should be >= 10 to force update over v9")

        val link = update["link"]?.jsonPrimitive?.content
        assertNotNull(link)
        assertTrue(link.contains("cafebazaar"), "Link should direct to Cafe Bazaar")
    }

    @Test
    fun testV1ConfigGetReturnsValidConfig() = testApplication {
        application {
            testModule(dropTables = false)
        }

        val response = client.get("/api/v1/config")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val parsed = json.parseToJsonElement(body).jsonObject
        val data = parsed["data"]?.jsonObject
        assertNotNull(data)
        val tokenAlive = data["tokenAlive"]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
        assertEquals(true, tokenAlive)
    }
}
