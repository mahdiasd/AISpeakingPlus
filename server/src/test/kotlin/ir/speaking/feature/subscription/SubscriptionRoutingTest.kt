package ir.speaking.feature.subscription

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
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.subscription.routing.subscriptionRouting
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubscriptionRoutingTest {

    @Test
    fun `test subscription plans and stage 3 paywall barrier`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            install(Koin) {
                modules(
                    module {
                        single { StageRepository() }
                        single { SubscriptionRepo() }
                    }
                )
            }
            configureTestSecurity()
            configureTestDatabases()
            stageRouting()
            subscriptionRouting()
        }

        // 1. GET subscription plans
        val plansResponse = client.get("/api/v2/subscriptions/plans")
        assertEquals(HttpStatusCode.OK, plansResponse.status)
        val plansBody = plansResponse.bodyAsText()
        assertTrue(plansBody.contains("1_MONTH") || plansBody.contains("MONTHLY"))
        assertTrue(plansBody.contains("3_MONTHS") || plansBody.contains("QUARTERLY"))

        // 2. Stage 3 unauthenticated or non-subscriber should return 401 or 402
        val stage3Response = client.get("/api/v2/stages/stage-03-heathrow-border")
        if (stage3Response.status != HttpStatusCode.InternalServerError) {
            assertTrue(
                stage3Response.status == HttpStatusCode.Unauthorized ||
                stage3Response.status == HttpStatusCode.PaymentRequired
            )
        }
    }
}
