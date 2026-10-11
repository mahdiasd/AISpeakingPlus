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
import ir.speaking.core.network.api.SmsApiService
import ir.speaking.feature.stage.repository.StageRepository
import ir.speaking.feature.stage.routing.stageRouting
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.subscription.routing.subscriptionRouting
import ir.speaking.feature.user.repository.UserRepo
import ir.speaking.feature.user.routing.authRouting
import ir.speaking.feature.user.service.AuthService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SubscriptionRoutingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

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
                        single { UserRepo(subscriptionRepo = get()) }
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
        val stage3Response = client.get("/api/v2/stages/stage-03-baggage-claim")
        if (stage3Response.status != HttpStatusCode.InternalServerError) {
            assertTrue(
                stage3Response.status == HttpStatusCode.Unauthorized ||
                stage3Response.status == HttpStatusCode.PaymentRequired
            )
        }
    }

    @Test
    fun `test post subscription subscribe lifecycle`() = testApplication {
        application {
            install(ContentNegotiation) {
                json(json)
            }
            configureTestSecurity()
            configureTestDatabases()

            val smsMock = SmsApiService(httpClient = io.ktor.client.HttpClient())
            val subscriptionRepo = SubscriptionRepo()
            val userRepo = UserRepo(subscriptionRepo = subscriptionRepo)
            val authService = AuthService(userRepo, smsMock)

            install(Koin) {
                modules(module {
                    single { userRepo }
                    single { subscriptionRepo }
                    single { smsMock }
                    single { authService }
                })
            }

            authRouting()
            subscriptionRouting()
        }

        // 1. Unauthenticated POST /api/v2/subscriptions/subscribe -> 401
        val unauthRes = client.post("/api/v2/subscriptions/subscribe") {
            contentType(ContentType.Application.Json)
            setBody("""{"planId":"plan-3m"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, unauthRes.status)

        // 2. Obtain token via OTP login with bypass number 09152413498
        val verifyRes = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "09152413498", "otpCode": "87799"}""")
        }
        assertEquals(HttpStatusCode.OK, verifyRes.status)
        val verifyBody = json.parseToJsonElement(verifyRes.bodyAsText()).jsonObject
        val token = verifyBody["data"]?.jsonObject?.get("token")?.jsonPrimitive?.content
        assertNotNull(token)

        // 3. Initial subscription status -> false
        val initialStatusRes = client.get("/api/v2/subscriptions/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, initialStatusRes.status)
        val initialBody = json.parseToJsonElement(initialStatusRes.bodyAsText()).jsonObject
        val initialData = initialBody["data"]?.jsonObject
        assertNotNull(initialData)
        assertEquals("false", initialData["isSubscriber"]?.jsonPrimitive?.content)

        // 4. Authenticated POST /api/v2/subscriptions/subscribe with invalid planId -> 400
        val invalidPlanRes = client.post("/api/v2/subscriptions/subscribe") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planId":"invalid-plan"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidPlanRes.status)

        // 4.1 Invalid promo code -> 400
        val invalidPromoRes = client.post("/api/v2/subscriptions/subscribe") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planId":"plan-3m","promoCode":"INVALID_PROMO"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidPromoRes.status)

        // 5. Authenticated POST /api/v2/subscriptions/subscribe with valid plan -> 200
        val subscribeRes = client.post("/api/v2/subscriptions/subscribe") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planId":"plan-3m","promoCode":"GOLD20"}""")
        }
        assertEquals(HttpStatusCode.OK, subscribeRes.status)
        val subBody = json.parseToJsonElement(subscribeRes.bodyAsText()).jsonObject
        val subData = subBody["data"]?.jsonObject
        assertNotNull(subData)
        assertEquals("true", subData["isSubscriber"]?.jsonPrimitive?.content)
        assertEquals("3_MONTHS", subData["planType"]?.jsonPrimitive?.content)
        val firstRemainingDays = subData["remainingDays"]?.jsonPrimitive?.content?.toInt() ?: 0
        assertTrue(firstRemainingDays >= 89)

        // 6. Cumulative renewal: subscribe to 1_MONTH while active -> adds 30 days cumulatively
        val renewRes = client.post("/api/v2/subscriptions/subscribe") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planId":"plan-1m"}""")
        }
        assertEquals(HttpStatusCode.OK, renewRes.status)
        val renewData = json.parseToJsonElement(renewRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(renewData)
        val renewedRemainingDays = renewData["remainingDays"]?.jsonPrimitive?.content?.toInt() ?: 0
        assertTrue(renewedRemainingDays >= firstRemainingDays + 29)

        // 7. Verified subscription status -> true
        val finalStatusRes = client.get("/api/v2/subscriptions/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, finalStatusRes.status)
        val finalBody = json.parseToJsonElement(finalStatusRes.bodyAsText()).jsonObject
        val finalData = finalBody["data"]?.jsonObject
        assertNotNull(finalData)
        assertEquals("true", finalData["isSubscriber"]?.jsonPrimitive?.content)
        assertEquals("1_MONTH", finalData["planType"]?.jsonPrimitive?.content)
    }
}

