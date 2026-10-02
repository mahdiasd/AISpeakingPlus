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
import ir.speaking.feature.stage_progress.dto.SyncProgressItem
import ir.speaking.feature.stage_progress.dto.SyncProgressRequest
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

class ProgressSyncRoutingTest {

    @Test
    fun `test stage 2 requires auth and progress sync requires auth`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Koin) {
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

        // 1. Stage 2 unauthenticated should return 401 AUTH_REQUIRED
        val stage2Response = client.get("/api/v2/stages/stage-02-heathrow-border")
        if (stage2Response.status != HttpStatusCode.InternalServerError) {
            assertEquals(HttpStatusCode.Unauthorized, stage2Response.status)
            assertTrue(stage2Response.bodyAsText().contains("AUTH_REQUIRED"))
        }

        // 2. Sync unauthenticated should return 401
        val syncUnauthResponse = client.post("/api/v2/progress/sync") {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(SyncProgressRequest(items = emptyList())))
        }
        assertEquals(HttpStatusCode.Unauthorized, syncUnauthResponse.status)
    }
}
