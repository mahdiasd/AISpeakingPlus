package ir.speaking.feature.user

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
import ir.speaking.feature.user.repository.UserRepo
import ir.speaking.feature.user.routing.authRouting
import ir.speaking.feature.user.service.AuthService
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals

class AuthRoutingTest {

    @Test
    fun testSendOtpFormats() = testApplication {
        application {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    explicitNulls = false
                })
            }
            configureTestSecurity()
            configureTestDatabases()

            val smsMock = SmsApiService(httpClient = io.ktor.client.HttpClient())
            val userRepo = UserRepo()
            val authService = AuthService(userRepo, smsMock)

            install(Koin) {
                modules(module {
                    single { userRepo }
                    single { smsMock }
                    single { authService }
                })
            }

            authRouting()
        }

        // Test 1: standard json {"mobile": "09152413498"}
        val res1 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "09152413498"}""")
        }
        assertEquals(HttpStatusCode.OK, res1.status)

        // Test 2: raw text body "09152413498"
        val res2 = client.post("/api/v2/auth/otp/send") {
            setBody("09152413498")
        }
        println("RES 2 BODY: " + res2.bodyAsText().replace("\n", " "))
        assertEquals(HttpStatusCode.OK, res2.status)

        // Test 3: json with number {"mobile": 9152413498}
        val res3 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": 9152413498}""")
        }
        assertEquals(HttpStatusCode.OK, res3.status)

        // Test 4: json with phoneNumber {"phoneNumber": "09152413498"}
        val res4 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"phoneNumber": "09152413498"}""")
        }
        assertEquals(HttpStatusCode.OK, res4.status)

        // Test 5: json with phone {"phone": "09152413498"}
        val res5 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"phone": "09152413498"}""")
        }
        assertEquals(HttpStatusCode.OK, res5.status)

        // Test 6: json with mobile_number {"mobile_number": "09152413498"}
        val res6 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile_number": "09152413498"}""")
        }
        assertEquals(HttpStatusCode.OK, res6.status)

        // Test 7: Persian digits {"mobile": "۰۹۱۵۲۴۱۳۴۹۸"}
        val res7 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "۰۹۱۵۲۴۱۳۴۹۸"}""")
        }
        assertEquals(HttpStatusCode.OK, res7.status)

        // Test 8: International prefix {"mobile": "+989152413498"}
        val res8 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "+989152413498"}""")
        }
        assertEquals(HttpStatusCode.OK, res8.status)

        // Test 9: Formatted with spaces/dashes {"mobile": "0915-241-3498"}
        val res9 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "0915-241-3498"}""")
        }
        assertEquals(HttpStatusCode.OK, res9.status)

        // Test 10: Query param ?mobile=09152413498 with empty body
        val res10 = client.post("/api/v2/auth/otp/send?mobile=09152413498")
        assertEquals(HttpStatusCode.OK, res10.status)

        // Test 11: empty body {} -> 400 Bad Request
        val res11 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{}""")
        }
        assertEquals(HttpStatusCode.BadRequest, res11.status)

        // Test 12: invalid phone number (too short) -> 400 Bad Request
        val res12 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "12345"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, res12.status)

        // Test 13: Verify OTP with debug/sandbox code
        val res13 = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "09120000000", "otpCode": "87799"}""")
        }
        println("RES 13 BODY: " + res13.bodyAsText())
        assertEquals(HttpStatusCode.OK, res13.status)

        // Test 14: Verify OTP with Persian digits
        val res14 = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "۰۹۱۲۰۰۰۰۰۰۰", "otpCode": "۸۷۷۹۹"}""")
        }
        assertEquals(HttpStatusCode.OK, res14.status)
    }
}
