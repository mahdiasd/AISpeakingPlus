package ir.speaking.core

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenApiRoutingTest {

    @Test
    fun `test openapi endpoint returns valid json specification`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(WebSockets)
            install(Koin) {
                modules(module {
                    // Empty or lazy module since we only test route tree traversal and OpenAPI spec generation
                })
            }
            configureTestSecurity()
            configureRouting()
        }

        // Test GET /health
        val healthResponse = client.get("/health")
        assertEquals(HttpStatusCode.OK, healthResponse.status)

        // Test GET /openapi
        val openApiResponse = client.get("/openapi")
        println("OpenAPI response status: ${openApiResponse.status}")
        println("OpenAPI content-type: ${openApiResponse.contentType()}")
        val openApiBody = openApiResponse.bodyAsText()
        println("OpenAPI body preview: ${openApiBody.take(200)}")

        // Verify status code is OK and Content-Type is JSON
        assertEquals(HttpStatusCode.OK, openApiResponse.status)
        assertTrue(openApiResponse.contentType()?.match(ContentType.Application.Json) == true)

        // Parse and validate OpenAPI 3.0 specification JSON
        val jsonElement = Json.parseToJsonElement(openApiBody)
        assertTrue(jsonElement is JsonObject)
        val jsonObject = jsonElement.jsonObject
        assertEquals("3.0.3", jsonObject["openapi"]?.jsonPrimitive?.content)
        assertTrue(jsonObject.containsKey("info"))
        assertTrue(jsonObject.containsKey("paths"))

        val paths = jsonObject["paths"]?.jsonObject
        assertNotNull(paths, "paths object must not be null")
        java.io.File("build/openapi-dump.json").writeText(openApiBody)

        // Verify that all required endpoints are documented in OpenAPI paths
        val expectedEndpoints = listOf(
            "/api/v2/auth/otp/send",
            "/api/v2/auth/otp/verify",
            "/api/v2/auth/me",
            "/api/v2/stages",
            "/api/v2/stages/{stageId}",
            "/api/v2/stages/{stageId}/hint",
            "/api/v2/stages/{stageId}/evaluate",
            "/api/v2/progress/sync",
            "/api/v2/subscriptions/plans",
            "/api/v2/subscriptions/status",
            "/api/v2/leaderboard/journey",
            "/api/v2/tts/synthesize",
            "/api/v2/tts/voices",
            "/api/v2/tts/speak",
            "/api/v2/tts/audio/{filename}",
            "/api/v2/stt/live",
            "/health"
        )

        for (endpoint in expectedEndpoints) {
            assertTrue(
                paths.containsKey(endpoint),
                "OpenAPI specification should contain route: $endpoint. Present routes: ${paths.keys}"
            )
        }

        // Verify Auth endpoints contain requestBody definitions
        val sendOtpPath = paths["/api/v2/auth/otp/send"]?.jsonObject?.get("post")?.jsonObject
        assertNotNull(sendOtpPath, "POST /api/v2/auth/otp/send must exist")
        assertTrue(sendOtpPath.containsKey("requestBody"), "POST /api/v2/auth/otp/send must have requestBody")

        val verifyOtpPath = paths["/api/v2/auth/otp/verify"]?.jsonObject?.get("post")?.jsonObject
        assertNotNull(verifyOtpPath, "POST /api/v2/auth/otp/verify must exist")
        assertTrue(verifyOtpPath.containsKey("requestBody"), "POST /api/v2/auth/otp/verify must have requestBody")

        // Test GET /openapi.json
        val openApiJsonAliasResponse = client.get("/openapi.json")
        assertEquals(HttpStatusCode.OK, openApiJsonAliasResponse.status)
        assertTrue(openApiJsonAliasResponse.contentType()?.match(ContentType.Application.Json) == true)

        // Test GET /swagger (Swagger UI in browser)
        val swaggerResponse = client.get("/swagger")
        assertEquals(HttpStatusCode.OK, swaggerResponse.status)
    }
}
