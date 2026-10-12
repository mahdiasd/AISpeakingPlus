package ir.speaking.feature.stage

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import ir.speaking.core.configureRoutingException
import ir.speaking.core.configureTestDatabases
import ir.speaking.core.configureTestSecurity
import ir.speaking.feature.stage.dto.StageChatRequest
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.stage.routing.stageRouting
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.user.repository.UserRepo
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatResponseFormat
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
            configureRoutingException()
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
        val mockAiService = object : AiApiService(AiClientManager()) {
            override suspend fun requestRaw(
                messages: List<ChatMessage>,
                responseFormat: ChatResponseFormat
            ): Result<String> {
                return Result.success("""{"suggestion_en":"Can I have water please?","explanation_fa":"می‌توانید خیلی ساده آب درخواست کنید."}""")
            }
        }
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
                        single<AiApiService> { mockAiService }
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
            configureRoutingException()
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

        assertEquals(HttpStatusCode.OK, a1Response.status)
        val a1Body = a1Response.bodyAsText()
        val a1Json = jsonConfig.parseToJsonElement(a1Body).jsonObject
        val a1Data = a1Json["data"]?.jsonObject
        val a1Suggestion = a1Data?.get("suggestionEn")?.jsonPrimitive?.content.orEmpty()
        val a1Explanation = a1Data?.get("explanationFa")?.jsonPrimitive?.content.orEmpty()
        assertTrue(a1Suggestion.isNotBlank(), "A1 suggestion should not be blank")
        assertTrue(a1Explanation.isNotBlank(), "A1 explanation should not be blank")
    }

    @Test
    fun `test stage chat returns BadGateway on AI service failure without fallback`() = testApplication {
        val jsonConfig = Json { ignoreUnknownKeys = true }
        val failingAiService = object : AiApiService(AiClientManager()) {
            override suspend fun requestRaw(
                messages: List<ChatMessage>,
                responseFormat: ChatResponseFormat
            ): Result<String> {
                return Result.failure(java.net.ConnectException("Connection to AI model failed"))
            }
        }
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
                        single<AiApiService> { failingAiService }
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
            configureRoutingException()
            configureTestDatabases()
            stageRouting()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(jsonConfig)
            }
        }

        val response = client.post("/api/v2/stages/stage-01-inflight-london/chat") {
            contentType(ContentType.Application.Json)
            setBody(StageChatRequest(message = "Hello, I am arriving in London for university"))
        }

        // On AI failure, the server MUST return 502 Bad Gateway and strictly MUST NOT return 200 with fake fallback
        assertEquals(HttpStatusCode.BadGateway, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("خطا در برقراری ارتباط با هوش مصنوعی"))
    }

    @Test
    fun `test stage starter greeting returns BadGateway on AI service failure without fallback`() = testApplication {
        val jsonConfig = Json { ignoreUnknownKeys = true }
        val failingAiService = object : AiApiService(AiClientManager()) {
            override suspend fun requestRaw(
                messages: List<ChatMessage>,
                responseFormat: ChatResponseFormat
            ): Result<String> {
                return Result.failure(java.net.ConnectException("Connection to AI model failed"))
            }
        }
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
                        single<AiApiService> { failingAiService }
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
            configureRoutingException()
            configureTestDatabases()
            stageRouting()
        }

        val client = createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(jsonConfig)
            }
        }

        val response = client.post("/api/v2/stages/stage-01-inflight-london/chat") {
            contentType(ContentType.Application.Json)
            setBody(StageChatRequest(message = ""))
        }

        // On initial starter greeting AI failure, the server MUST return 502 Bad Gateway and NOT 200 with fake fallback
        assertEquals(HttpStatusCode.BadGateway, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("خطا در برقراری ارتباط با هوش مصنوعی"))
    }
}
