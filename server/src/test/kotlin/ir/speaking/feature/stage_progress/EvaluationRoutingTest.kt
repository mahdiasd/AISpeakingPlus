package ir.speaking.feature.stage_progress

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import ir.speaking.core.configureTestDatabases
import ir.speaking.core.configureTestSecurity
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.stage.routing.stageRouting
import ir.speaking.feature.stage_progress.dto.EvaluationRequest
import ir.speaking.feature.stage_progress.dto.EvaluationTranscriptItem
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.stage_progress.routing.progressRouting
import ir.speaking.feature.stage_progress.service.EvaluationRubricService
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.user.repository.UserRepo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EvaluationRoutingTest {

    @Test
    fun `test hint and evaluate routing`() = testApplication {
        application {
            this@application.install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            this@application.install(Koin) {
                modules(
                    module {
                        single { StageRepository() }
                        single { StageProgressRepo() }
                        single { EvaluationRubricService() }
                        single { SubscriptionRepo() }
                        single { UserRepo(subscriptionRepo = get()) }
                    }
                )
            }
            configureTestSecurity()
            configureTestDatabases()
            stageRouting()
            progressRouting()
        }

        // 1. Test POST hint
        val hintResponse = client.post("/api/v2/stages/stage-01-inflight-london/hint") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        if (hintResponse.status == HttpStatusCode.OK) {
            val body = hintResponse.bodyAsText()
            assertTrue(body.contains("suggestionEn"))
            assertTrue(body.contains("explanationFa"))
        }

        // 2. Test POST evaluate with objectiveCompleted = true
        val evalReq = EvaluationRequest(
            hintsUsedCount = 0,
            turnsCount = 4,
            objectiveCompleted = true,
            transcript = listOf(
                EvaluationTranscriptItem("Model", "Hello! Welcome aboard. Would you like chicken or pasta?"),
                EvaluationTranscriptItem("User", "I would like chicken with rice and orange juice, please.")
            )
        )
        val evalResponse = client.post("/api/v2/stages/stage-01-inflight-london/evaluate") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(evalReq))
        }
        if (evalResponse.status == HttpStatusCode.OK) {
            val body = evalResponse.bodyAsText()
            assertTrue(body.contains("starsEarned"))
            assertTrue(body.contains("feedbackFa"))
            val parsed = Json.decodeFromString<ir.speaking.core.response.SuccessResponse<ir.speaking.feature.stage_progress.dto.EvaluationResponse>>(body)
            assertEquals(3, parsed.data?.starsEarned)
            assertEquals(0, parsed.data?.grammarErrorsCount)
        }

        // 3. Test POST evaluate with AI-identified grammar errors and objectiveCompleted = true
        val evalReqWithErrors = EvaluationRequest(
            hintsUsedCount = 0,
            turnsCount = 4,
            objectiveCompleted = true,
            grammarErrorsCount = 2,
            grammarErrors = listOf(
                ir.speaking.feature.stage_progress.dto.GrammarErrorItem(
                    original = "I wants pasta",
                    correction = "I want pasta",
                    explanationFa = "اشکال در فاعل و فعل"
                ),
                ir.speaking.feature.stage_progress.dto.GrammarErrorItem(
                    original = "I flying tomorrow",
                    correction = "I am flying tomorrow",
                    explanationFa = "اشکال در زمان استمراری"
                )
            ),
            transcript = listOf(
                EvaluationTranscriptItem("Model", "Hello!"),
                EvaluationTranscriptItem("User", "I wants pasta")
            )
        )
        val evalResponseErrors = client.post("/api/v2/stages/stage-01-inflight-london/evaluate") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(evalReqWithErrors))
        }
        assertEquals(HttpStatusCode.OK, evalResponseErrors.status)
        val bodyErrors = evalResponseErrors.bodyAsText()
        val parsedErrors = Json.decodeFromString<ir.speaking.core.response.SuccessResponse<ir.speaking.feature.stage_progress.dto.EvaluationResponse>>(bodyErrors)
        assertEquals(2, parsedErrors.data?.grammarErrorsCount)
        assertEquals(1, parsedErrors.data?.starsEarned) // 2 penalties = 1 star
        assertEquals(2, parsedErrors.data?.totalPenalties)
        assertEquals(2, parsedErrors.data?.grammarErrors?.size)

        // 4. Test POST evaluate with objectiveCompleted = false -> awards 0 stars and 0 score
        val evalReqIncomplete = EvaluationRequest(
            hintsUsedCount = 0,
            turnsCount = 2,
            objectiveCompleted = false,
            transcript = listOf(
                EvaluationTranscriptItem("Model", "Welcome!"),
                EvaluationTranscriptItem("User", "Hello there")
            )
        )
        val evalResponseIncomplete = client.post("/api/v2/stages/stage-01-inflight-london/evaluate") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(evalReqIncomplete))
        }
        assertEquals(HttpStatusCode.OK, evalResponseIncomplete.status)
        val bodyIncomplete = evalResponseIncomplete.bodyAsText()
        val parsedIncomplete = Json.decodeFromString<ir.speaking.core.response.SuccessResponse<ir.speaking.feature.stage_progress.dto.EvaluationResponse>>(bodyIncomplete)
        assertEquals(0, parsedIncomplete.data?.starsEarned)
        assertEquals(0, parsedIncomplete.data?.score)
        assertEquals(false, parsedIncomplete.data?.objectiveCompleted)
    }
}
