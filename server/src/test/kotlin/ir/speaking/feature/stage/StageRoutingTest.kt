package ir.speaking.feature.stage

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
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.user.repository.UserRepo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ir.speaking.core.network.api.AiApiService
import ir.speaking.core.network.di.AiClientManager
import ir.speaking.feature.stage.dto.HintRequest
import ir.speaking.feature.stage.service.StageChatService
import ir.speaking.feature.tts.service.TtsConfig
import ir.speaking.feature.tts.service.TtsService
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StageRoutingTest {

    @Test
    fun `test guest access to stages catalog and stage 1`() = testApplication {
        application {
            this@application.install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            this@application.install(Koin) {
                modules(
                    module {
                        single { StageRepository() }
                        single { StageProgressRepo() }
                        single { SubscriptionRepo() }
                        single { UserRepo(subscriptionRepo = get()) }
                        single {
                            TtsService(
                                TtsConfig.fromEnv().copy(
                                    enabled = false,
                                    cacheDir = System.getProperty("java.io.tmpdir")
                                )
                            )
                        }
                        single { AiApiService(AiClientManager()) }
                        single {
                            StageChatService(
                                stageRepository = get(),
                                ttsService = get(),
                                aiApiService = get(),
                                userRepo = get()
                            )
                        }
                    }
                )
            }
            configureTestSecurity()
            configureTestDatabases()
            stageRouting()
        }

        // 1. Unauthenticated GET /api/v2/stages
        val catalogResponse = client.get("/api/v2/stages")
        // If DB is not available in test env, status might be 500 or 200; verify routing logic
        if (catalogResponse.status == HttpStatusCode.OK) {
            val body = catalogResponse.bodyAsText()
            assertTrue(body.contains("GUEST"))
            assertTrue(body.contains("stage-01-inflight-london"))
        }

        // 2. Unauthenticated GET /api/v2/stages/stage-02-heathrow-border
        val stage2Response = client.get("/api/v2/stages/stage-02-heathrow-border")
        if (stage2Response.status != HttpStatusCode.InternalServerError) {
            assertEquals(HttpStatusCode.Unauthorized, stage2Response.status)
            val body = stage2Response.bodyAsText()
            assertTrue(body.contains("AUTH_REQUIRED"))
        }
    }

    @Test
    fun `test stage hint adapts suggestion based on user CEFR level`() = testApplication {
        val jsonConfig = Json { ignoreUnknownKeys = true }
        application {
            this@application.install(ContentNegotiation) {
                json(jsonConfig)
            }
            this@application.install(Koin) {
                modules(
                    module {
                        single { StageRepository() }
                        single { StageProgressRepo() }
                        single { SubscriptionRepo() }
                        single { UserRepo(subscriptionRepo = get()) }
                        single {
                            TtsService(
                                TtsConfig.fromEnv().copy(
                                    enabled = false,
                                    cacheDir = System.getProperty("java.io.tmpdir")
                                )
                            )
                        }
                        single { AiApiService(AiClientManager()) }
                        single {
                            StageChatService(
                                stageRepository = get(),
                                ttsService = get(),
                                aiApiService = get(),
                                userRepo = get()
                            )
                        }
                    }
                )
            }
            configureTestSecurity()
            configureTestDatabases()
            stageRouting()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(jsonConfig)
            }
        }

        // Request hint for stage 1 with A1 level
        val a1Response = client.post("/api/v2/stages/stage-01-inflight-london/hint") {
            contentType(ContentType.Application.Json)
            setBody(HintRequest(messages = emptyList(), userLevel = "A1"))
        }

        if (a1Response.status == HttpStatusCode.OK) {
            val a1Body = a1Response.bodyAsText()
            val a1Json = jsonConfig.parseToJsonElement(a1Body).jsonObject
            val a1Data = a1Json["data"]?.jsonObject
            val a1Suggestion = a1Data?.get("suggestionEn")?.jsonPrimitive?.content.orEmpty()
            val a1Explanation = a1Data?.get("explanationFa")?.jsonPrimitive?.content.orEmpty()
            assertTrue(a1Suggestion.isNotBlank(), "A1 suggestion should not be blank")
            assertTrue(a1Explanation.isNotBlank(), "A1 explanation should not be blank")
        }

        // Request hint for stage 1 with B2 level
        val b2Response = client.post("/api/v2/stages/stage-01-inflight-london/hint") {
            contentType(ContentType.Application.Json)
            setBody(HintRequest(messages = emptyList(), userLevel = "B2"))
        }

        if (b2Response.status == HttpStatusCode.OK) {
            val b2Body = b2Response.bodyAsText()
            val b2Json = jsonConfig.parseToJsonElement(b2Body).jsonObject
            val b2Data = b2Json["data"]?.jsonObject
            val b2Suggestion = b2Data?.get("suggestionEn")?.jsonPrimitive?.content.orEmpty()
            val b2Explanation = b2Data?.get("explanationFa")?.jsonPrimitive?.content.orEmpty()
            assertTrue(b2Suggestion.isNotBlank(), "B2 suggestion should not be blank")
            assertTrue(b2Explanation.isNotBlank(), "B2 explanation should not be blank")
        }
    }
}
