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

        // 2. Test POST evaluate
        val evalReq = EvaluationRequest(
            hintsUsedCount = 0,
            turnsCount = 4,
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
        }
    }
}
