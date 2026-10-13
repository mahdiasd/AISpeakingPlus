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
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthSecurityTest {

    @Test
    fun `test OTP send cooldown rejects duplicate requests within 60 seconds`() = testApplication {
        val authService = AuthService(UserRepo(), SmsApiService(httpClient = io.ktor.client.HttpClient()))
        authService.resetCachesForTesting()

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureTestSecurity()
            configureTestDatabases()

            install(Koin) {
                modules(module {
                    single { UserRepo() }
                    single { SmsApiService(httpClient = io.ktor.client.HttpClient()) }
                    single { authService }
                })
            }
            authRouting()
        }

        val testMobile = "09121112233"

        // 1. First send OTP request -> 200 OK
        val res1 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile"}""")
        }
        assertEquals(HttpStatusCode.OK, res1.status)

        // 2. Second send OTP request immediately -> 429 Too Many Requests (Cooldown active)
        val res2 = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile"}""")
        }
        assertEquals(HttpStatusCode.TooManyRequests, res2.status)
        val body2 = res2.bodyAsText()
        assertTrue(body2.contains("۶۰ ثانیه") || body2.contains("صبر کنید"), "Should mention 60s cooldown: $body2")
    }

    @Test
    fun `test OTP verify brute force locks mobile for 5 minutes after 3 failed attempts`() = testApplication {
        val authService = AuthService(UserRepo(), SmsApiService(httpClient = io.ktor.client.HttpClient()))
        authService.resetCachesForTesting()

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureTestSecurity()
            configureTestDatabases()

            install(Koin) {
                modules(module {
                    single { UserRepo() }
                    single { SmsApiService(httpClient = io.ktor.client.HttpClient()) }
                    single { authService }
                })
            }
            authRouting()
        }

        val testMobile = "09123334455"

        // Send OTP first
        val sendRes = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile"}""")
        }
        assertEquals(HttpStatusCode.OK, sendRes.status)

        // 1st failed attempt -> 401 Unauthorized
        val attempt1 = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile", "otpCode": "00001"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, attempt1.status)

        // 2nd failed attempt -> 401 Unauthorized
        val attempt2 = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile", "otpCode": "00002"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, attempt2.status)

        // 3rd failed attempt -> 429 Too Many Requests (Account blocked)
        val attempt3 = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile", "otpCode": "00003"}""")
        }
        assertEquals(HttpStatusCode.TooManyRequests, attempt3.status)
        assertTrue(attempt3.bodyAsText().contains("مسدود"), "Should indicate account is blocked")

        // 4th attempt -> 429 Too Many Requests immediately because mobile is blocked
        val attempt4 = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile", "otpCode": "00004"}""")
        }
        assertEquals(HttpStatusCode.TooManyRequests, attempt4.status)

        // Also trying to send OTP while blocked should return 429
        val sendWhileBlocked = client.post("/api/v2/auth/otp/send") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "$testMobile"}""")
        }
        assertEquals(HttpStatusCode.TooManyRequests, sendWhileBlocked.status)
    }

    @Test
    fun `test logout blacklists JWT token and prevents subsequent access`() = testApplication {
        val userRepo = UserRepo()
        val authService = AuthService(userRepo, SmsApiService(httpClient = io.ktor.client.HttpClient()))
        authService.resetCachesForTesting()

        application {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
            configureTestSecurity()
            configureTestDatabases()

            install(Koin) {
                modules(module {
                    single { userRepo }
                    single { SmsApiService(httpClient = io.ktor.client.HttpClient()) }
                    single { authService }
                })
            }
            authRouting()
        }

        // Login with bypass user
        val loginRes = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "09152413498", "otpCode": "87799"}""")
        }
        assertEquals(HttpStatusCode.OK, loginRes.status)

        val json = Json { ignoreUnknownKeys = true }
        val loginBody = loginRes.bodyAsText()
        val token = json.parseToJsonElement(loginBody).jsonObject["data"]?.jsonObject?.get("token")?.jsonPrimitive?.content
        assertNotNull(token, "Token should be present")

        // 1. Authenticated call to /api/v2/auth/me succeeds with token
        val meRes1 = client.get("/api/v2/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, meRes1.status)

        // 2. Logout call invalidates the token
        val logoutRes = client.post("/api/v2/auth/logout") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, logoutRes.status)

        // 3. Subsequent call to /api/v2/auth/me with the same token MUST return 401 Unauthorized
        val meRes2 = client.get("/api/v2/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.Unauthorized, meRes2.status)
    }
}
