package ir.speaking.feature.stage

import com.aallam.openai.api.chat.ChatMessage
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
import ir.speaking.core.network.api.AiApiService
import ir.speaking.core.network.di.AiClientManager
import ir.speaking.feature.stage.dto.StageChatRequest
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.stage.routing.stageRouting
import ir.speaking.feature.stage.service.StageChatService
import ir.speaking.feature.stage_progress.repository.StageProgressRepo
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.tts.service.TtsConfig
import ir.speaking.feature.tts.service.TtsService
import ir.speaking.feature.user.repository.UserRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StageChatStreamRoutingTest {

    @Test
    fun `test stage chat stream endpoint emits sse chunks and done event`() = testApplication {
        val jsonConfig = Json { ignoreUnknownKeys = true }

        val streamingMockAiService = object : AiApiService(AiClientManager()) {
            override fun streamRequest(
                messages: List<ChatMessage>,
                responseFormat: com.aallam.openai.api.chat.ChatResponseFormat
            ): Flow<String> = flow {
                emit("{\"reply\": \"Hello ")
                emit("welcome ")
                emit("aboard!\", \"translated_reply\": \"سلام به پرواز خوش آمدید!\", \"has_grammar_error\": false, \"objective_completed\": true}")
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
                        single<AiApiService> { streamingMockAiService }
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

        // Call POST /api/v2/stages/stage-01-inflight-london/chat/stream
        val streamResponse = client.post("/api/v2/stages/stage-01-inflight-london/chat/stream") {
            contentType(ContentType.Application.Json)
            setBody(
                StageChatRequest(
                    message = "Can I have some water?",
                    history = emptyList(),
                    userLevel = "A1"
                )
            )
        }

        assertEquals(HttpStatusCode.OK, streamResponse.status)
        val contentTypeHeader = streamResponse.headers[HttpHeaders.ContentType]
        assertTrue(contentTypeHeader?.contains("text/event-stream") == true, "Content-Type must be text/event-stream, got: $contentTypeHeader")

        val body = streamResponse.bodyAsText()
        assertTrue(body.contains("event: chunk") || body.contains("\"type\":\"chunk\""), "Body should contain chunk event: $body")
        assertTrue(body.contains("event: done") || body.contains("\"type\":\"done\""), "Body should contain done event: $body")
        assertTrue(body.contains("welcome") || body.contains("Hello"), "Body should contain streamed text: $body")
        assertTrue(body.contains("objective_completed"), "Done event payload should contain completion info")
    }
}
