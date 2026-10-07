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
import ir.speaking.feature.subscription.repository.SubscriptionRepo
import ir.speaking.feature.user.repository.UserRepo
import ir.speaking.feature.user.routing.authRouting
import ir.speaking.feature.user.routing.userRouting
import ir.speaking.feature.user.service.AuthService
import ir.speaking.core.network.api.SmsApiService
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UserProfileRoutingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    @Test
    fun `test profile lifecycle unauthorized get update and validation`() = testApplication {
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
            userRouting()
        }

        // 1. Unauthenticated GET /api/v2/user/profile -> 401 Unauthorized
        val unauthGet = client.get("/api/v2/user/profile")
        assertEquals(HttpStatusCode.Unauthorized, unauthGet.status)

        // 2. Unauthenticated PUT /api/v2/user/profile -> 401 Unauthorized
        val unauthPut = client.put("/api/v2/user/profile") {
            contentType(ContentType.Application.Json)
            setBody("""{"nickName":"NewName"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, unauthPut.status)

        // 3. Authenticate via OTP verify to obtain a valid JWT token
        val verifyRes = client.post("/api/v2/auth/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody("""{"mobile": "09121112233", "otpCode": "87799"}""")
        }
        assertEquals(HttpStatusCode.OK, verifyRes.status)
        val verifyBody = json.parseToJsonElement(verifyRes.bodyAsText()).jsonObject
        val dataObj = verifyBody["data"]?.jsonObject
        assertNotNull(dataObj, "data object should not be null")
        val token = dataObj["token"]?.jsonPrimitive?.content
        assertNotNull(token, "token should not be null")

        // 4. Authenticated GET /api/v2/user/profile -> 200 OK
        val profileRes = client.get("/api/v2/user/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, profileRes.status)
        val profileJson = json.parseToJsonElement(profileRes.bodyAsText()).jsonObject
        val profileData = profileJson["data"]?.jsonObject
        assertNotNull(profileData)
        assertEquals("09121112233", profileData["phoneNumber"]?.jsonPrimitive?.content)
        assertTrue(profileData.containsKey("totalStars"))
        assertTrue(profileData.containsKey("score"))
        assertTrue(profileData.containsKey("completedStagesCount"))
        assertTrue(profileData.containsKey("subscription"))

        // 5. Authenticated PUT /api/v2/user/profile with valid nickname & avatar -> 200 OK
        val updateRes = client.put("/api/v2/user/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"nickName": "کاربر تستی", "avatar": "avatar_g7"}""")
        }
        assertEquals(HttpStatusCode.OK, updateRes.status)
        val updatedProfileData = json.parseToJsonElement(updateRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(updatedProfileData)
        assertEquals("کاربر تستی", updatedProfileData["nickName"]?.jsonPrimitive?.content)
        assertEquals("avatar_g7", updatedProfileData["avatar"]?.jsonPrimitive?.content)

        // 5.1 Authenticated PUT /api/v2/user/profile with languageLevel -> 200 OK
        val updateLevelRes = client.put("/api/v2/user/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"languageLevel": "B2"}""")
        }
        assertEquals(HttpStatusCode.OK, updateLevelRes.status)
        val levelProfileData = json.parseToJsonElement(updateLevelRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(levelProfileData)
        assertEquals("B2", levelProfileData["languageLevel"]?.jsonPrimitive?.content)

        // 6. Validation error: nickname too short (< 2 characters) -> 400 Bad Request
        val invalidShortRes = client.put("/api/v2/user/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"nickName": "a"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidShortRes.status)

        // 7. Validation error: nickname too long (> 50 characters) -> 400 Bad Request
        val longNick = "a".repeat(51)
        val invalidLongRes = client.put("/api/v2/user/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"nickName": "$longNick"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidLongRes.status)
    }
}
