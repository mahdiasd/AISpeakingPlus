package ir.speaking.feature.placement

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import ir.speaking.core.configureTestDatabases
import ir.speaking.core.configureTestSecurity
import ir.speaking.feature.placement.dto.PlacementEvaluationResponseServerDto
import ir.speaking.feature.placement.dto.PlacementQuestionsResponseServerDto
import ir.speaking.feature.placement.dto.PlacementSubmitRequestServerDto
import ir.speaking.feature.placement.routing.placementRouting
import ir.speaking.feature.placement.service.PlacementEvaluationService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PlacementRoutingTest {

    private val testJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun `test placement questions generation and evaluation flow`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(testJson)
            }
            install(Koin) {
                modules(
                    module {
                        single { PlacementEvaluationService() }
                    }
                )
            }
            configureTestSecurity()
            configureTestDatabases()
            placementRouting()
        }

        // 1. Fetch 20 Placement Questions
        val getResponse = client.get("/api/v2/placement/questions")
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val getBody = getResponse.bodyAsText()

        assertTrue(getBody.contains("sessionId"), "Response should contain sessionId")
        assertTrue(getBody.contains("questions"), "Response should contain questions")

        // Parse questions
        val rootJson = testJson.parseToJsonElement(getBody).jsonObject
        val dataJson = rootJson["data"]?.toString() ?: ""
        val questionsResponse = testJson.decodeFromString<PlacementQuestionsResponseServerDto>(dataJson)

        assertEquals(20, questionsResponse.totalQuestions, "Should return 20 questions")
        assertEquals(20, questionsResponse.questions.size, "Questions list size should be 20")

        // Verify security: correctOption must NOT be leaked
        assertFalse(getBody.contains("correctOption"), "Question response must never leak correctOption")

        for (q in questionsResponse.questions) {
            assertEquals(4, q.options.size, "Each question must have exactly 4 options")
            assertTrue(q.questionText.isNotBlank(), "Question prompt must not be blank")
        }

        // 2. Submit Answers
        val firstFiveAnswers = questionsResponse.questions.take(5).associate { it.id to it.options.first() }
        val submitPayload = PlacementSubmitRequestServerDto(
            sessionId = questionsResponse.sessionId,
            answers = firstFiveAnswers
        )

        val postResponse = client.post("/api/v2/placement/evaluate") {
            contentType(ContentType.Application.Json)
            setBody(testJson.encodeToString(PlacementSubmitRequestServerDto.serializer(), submitPayload))
        }

        assertEquals(HttpStatusCode.OK, postResponse.status)
        val postBody = postResponse.bodyAsText()

        assertTrue(postBody.contains("assignedLevel"), "Response should contain assignedLevel")
        assertTrue(postBody.contains("scorePercent"), "Response should contain scorePercent")
        assertTrue(postBody.contains("reviewItems"), "Response should contain reviewItems")

        val postRootJson = testJson.parseToJsonElement(postBody).jsonObject
        val evalDataJson = postRootJson["data"]?.toString() ?: ""
        val evalResponse = testJson.decodeFromString<PlacementEvaluationResponseServerDto>(evalDataJson)

        assertNotNull(evalResponse.assignedLevel)
        assertEquals(20, evalResponse.reviewItems.size, "Review items count should match 20")
        assertTrue(evalResponse.correctCount + evalResponse.wrongCount == 20, "Sum of correct and wrong should equal 20")
    }
}
