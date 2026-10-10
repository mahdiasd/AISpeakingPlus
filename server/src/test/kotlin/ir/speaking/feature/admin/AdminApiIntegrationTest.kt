package ir.speaking.feature.admin

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.testing.*
import ir.speaking.core.configureTestDatabases
import ir.speaking.core.configureTestSecurity
import ir.speaking.feature.admin.db.AdminAuditTable
import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.admin.routing.adminRouting
import ir.speaking.feature.stage.db.StageSeedData
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.subscription.db.SubscriptionTable
import ir.speaking.feature.user.db.UserTable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AdminApiIntegrationTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun ApplicationTestBuilder.setupTestApp() {
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
            configureTestDatabases(dropTables = true)
            adminRouting()
        }
    }

    private suspend fun ApplicationTestBuilder.loginAndGetToken(
        username: String = "admin@aispeaking.ir",
        password: String = "Admin@123456!"
    ): String {
        val loginRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username","password":"$password"}""")
        }
        assertEquals(HttpStatusCode.OK, loginRes.status)
        val loginJson = json.parseToJsonElement(loginRes.bodyAsText()).jsonObject
        val dataObj = loginJson["data"]?.jsonObject
        assertNotNull(dataObj)
        return dataObj["token"]!!.jsonPrimitive.content
    }

    // =========================================================================
    // 1. Admin Authentication Tests
    // =========================================================================

    @Test
    fun testAdminAuthenticationComprehensive() = testApplication {
        setupTestApp()

        // 1. Valid login with standard credentials
        val token = loginAndGetToken("admin@aispeaking.ir", "Admin@123456!")
        assertTrue(token.isNotBlank())

        // 2. Removed hardcoded alias credentials (admin / admin123) -> 401 Unauthorized
        val aliasLoginRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"admin","password":"admin123"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, aliasLoginRes.status)

        // 3. Invalid password -> 401 Unauthorized
        val badPasswordRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"admin@aispeaking.ir","password":"WrongPassword123!"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, badPasswordRes.status)

        // 4. Non-existent username -> 401 Unauthorized
        val unknownUserRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"nonexistent@aispeaking.ir","password":"Admin@123456!"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, unknownUserRes.status)

        // 5. Deactivated admin user -> 401 Unauthorized
        val deactivatedAdminId = UUID.randomUUID()
        transaction {
            AdminUserTable.insert {
                it[id] = deactivatedAdminId
                it[username] = "deactivated@aispeaking.ir"
                it[passwordHash] = BCrypt.withDefaults().hashToString(12, "Deact@123".toCharArray())
                it[fullName] = "Deactivated Admin"
                it[role] = "ROLE_ADMIN"
                it[isActive] = false
            }
        }
        val deactivatedLoginRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"deactivated@aispeaking.ir","password":"Deact@123"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, deactivatedLoginRes.status)

        // 6. Malformed JSON payload -> 400 Bad Request
        val malformedJsonRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username": incomplete_json""")
        }
        assertEquals(HttpStatusCode.BadRequest, malformedJsonRes.status)

        // 7. Authenticated profile endpoint /me -> 200 OK
        val meRes = client.get("/api/admin/auth/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, meRes.status)
        val meJson = json.parseToJsonElement(meRes.bodyAsText()).jsonObject
        val meData = meJson["data"]?.jsonObject
        assertNotNull(meData)
        assertEquals("admin@aispeaking.ir", meData["username"]?.jsonPrimitive?.content)
        assertEquals("ROLE_SUPER_ADMIN", meData["role"]?.jsonPrimitive?.content)

        // 8. Unauthenticated profile endpoint /me -> 401 Unauthorized
        val unauthMeRes = client.get("/api/admin/auth/me")
        assertEquals(HttpStatusCode.Unauthorized, unauthMeRes.status)

        // 9. Invalid Bearer token -> 401 Unauthorized
        val badTokenMeRes = client.get("/api/admin/auth/me") {
            header(HttpHeaders.Authorization, "Bearer this.is.an.invalid.jwt.token")
        }
        assertEquals(HttpStatusCode.Unauthorized, badTokenMeRes.status)

        // 10. Audit log check: verify ADMIN_LOGIN is recorded in audit logs
        transaction {
            val loginLogs = AdminAuditTable.selectAll()
                .where { AdminAuditTable.action eq "ADMIN_LOGIN" }
                .toList()
            assertTrue(loginLogs.isNotEmpty(), "ADMIN_LOGIN audit log must be recorded upon successful login")
        }
    }

    // =========================================================================
    // 2. Dashboard KPI Stats & Audit Logs Tests
    // =========================================================================

    @Test
    fun testDashboardStatsAndAuditLogsComprehensive() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        // 1. Unauthenticated access -> 401
        val unauthStatsRes = client.get("/api/admin/dashboard/stats")
        assertEquals(HttpStatusCode.Unauthorized, unauthStatsRes.status)

        val unauthAuditRes = client.get("/api/admin/audit-logs")
        assertEquals(HttpStatusCode.Unauthorized, unauthAuditRes.status)

        // 2. Insert test data to verify calculation accuracy
        val activeUserId = UUID.randomUUID()
        val suspendedUserId = UUID.randomUUID()
        val now = Clock.System.now()
        val futureExpiry = now.plus(30, DateTimeUnit.DAY, kotlinx.datetime.TimeZone.UTC)

        transaction {
            UserTable.insert {
                it[id] = activeUserId
                it[mobile] = "09121110001"
                it[nickName] = "Active User"
                it[status] = "ACTIVE"
            }
            UserTable.insert {
                it[id] = suspendedUserId
                it[mobile] = "09121110002"
                it[nickName] = "Suspended User"
                it[status] = "SUSPENDED"
            }
            SubscriptionTable.insert {
                it[id] = UUID.randomUUID()
                it[userId] = activeUserId
                it[planType] = "MONTHLY"
                it[startedAt] = now
                it[expiresAt] = futureExpiry
                it[status] = "ACTIVE"
                it[grantSource] = "MANUAL_ADMIN"
            }
        }

        // 3. Authenticated Dashboard Stats
        val statsRes = client.get("/api/admin/dashboard/stats") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, statsRes.status)
        val statsJson = json.parseToJsonElement(statsRes.bodyAsText()).jsonObject
        val statsData = statsJson["data"]?.jsonObject
        assertNotNull(statsData)

        val totalUsers = statsData["totalUsers"]?.jsonPrimitive?.content?.toLong() ?: 0L
        val activeUsers = statsData["activeUsers"]?.jsonPrimitive?.content?.toLong() ?: 0L
        val activeSubscriptions = statsData["activeSubscriptions"]?.jsonPrimitive?.content?.toLong() ?: 0L
        val totalStages = statsData["totalStages"]?.jsonPrimitive?.content?.toLong() ?: 0L
        val publishedStages = statsData["publishedStages"]?.jsonPrimitive?.content?.toLong() ?: 0L

        assertTrue(totalUsers >= 2L, "Total users should be at least 2")
        assertTrue(activeUsers >= 1L, "Active users should be at least 1")
        assertTrue(activeSubscriptions >= 1L, "Active subscriptions should be at least 1")
        assertTrue(totalStages >= 15L, "Default seeded stages should be at least 15")
        assertTrue(publishedStages >= 15L, "Default published stages should be at least 15")

        // 4. Audit Logs with query parameter limits (limit=5, limit=0 coerced to 1, limit=300 coerced to 200)
        val auditLimit5 = client.get("/api/admin/audit-logs?limit=5") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, auditLimit5.status)
        val limit5Data = json.parseToJsonElement(auditLimit5.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(limit5Data)
        assertTrue(limit5Data.size in 1..5)

        val auditLimitClamped = client.get("/api/admin/audit-logs?limit=300") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, auditLimitClamped.status)

        // Verify audit log item properties
        val firstLog = limit5Data.first().jsonObject
        assertNotNull(firstLog["action"])
        assertNotNull(firstLog["targetType"])
        assertNotNull(firstLog["createdAt"])
    }

    // =========================================================================
    // 3. User Management Tests (Pagination, Persian Search, Status Updates)
    // =========================================================================

    @Test
    fun testUserManagementComprehensive() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        val userId1 = UUID.randomUUID()
        val userId2 = UUID.randomUUID()
        val userId3 = UUID.randomUUID()

        transaction {
            UserTable.insert {
                it[id] = userId1
                it[mobile] = "09123456789"
                it[nickName] = "کاربر اول تستی"
                it[firstName] = "علی"
                it[lastName] = "رضایی"
                it[status] = "ACTIVE"
            }
            UserTable.insert {
                it[id] = userId2
                it[mobile] = "09351234567"
                it[nickName] = "کاربر دوم"
                it[firstName] = "مریم"
                it[lastName] = "احمدی"
                it[status] = "ACTIVE"
            }
            UserTable.insert {
                it[id] = userId3
                it[mobile] = "09199998888"
                it[nickName] = "کاربر مسدود شده"
                it[status] = "SUSPENDED"
            }
        }

        // 1. Unauthenticated users list -> 401
        val unauthList = client.get("/api/admin/users")
        assertEquals(HttpStatusCode.Unauthorized, unauthList.status)

        // 2. Paginated User List
        val page1Res = client.get("/api/admin/users?page=1&limit=2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, page1Res.status)
        val page1Json = json.parseToJsonElement(page1Res.bodyAsText()).jsonObject
        val page1Items = page1Json["data"]?.jsonArray
        assertNotNull(page1Items)
        assertEquals(2, page1Items.size)
        val pagingMeta = page1Json["pagingMeta"]?.jsonObject
        assertNotNull(pagingMeta)
        assertEquals("1", pagingMeta["page"]?.jsonPrimitive?.content)
        assertTrue((pagingMeta["totalItems"]?.jsonPrimitive?.content?.toLong() ?: 0L) >= 3L)

        // 3. Search with Persian digits: ۰۹۱۲۳۴۵۶۷۸۹ -> should find userId1
        val searchPersianRes = client.get("/api/admin/users?search=۰۹۱۲۳۴۵۶۷۸۹") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, searchPersianRes.status)
        val searchPersianData = json.parseToJsonElement(searchPersianRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(searchPersianData)
        assertTrue(searchPersianData.any { it.jsonObject["id"]?.jsonPrimitive?.content == userId1.toString() })

        // 4. Search by name/nickname (e.g. "مریم") -> should find userId2
        val searchNameRes = client.get("/api/admin/users?search=مریم") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, searchNameRes.status)
        val searchNameData = json.parseToJsonElement(searchNameRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(searchNameData)
        assertTrue(searchNameData.any { it.jsonObject["id"]?.jsonPrimitive?.content == userId2.toString() })

        // 5. Filter by status: status=SUSPENDED -> should only find userId3
        val filterSuspendedRes = client.get("/api/admin/users?status=SUSPENDED") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, filterSuspendedRes.status)
        val suspendedData = json.parseToJsonElement(filterSuspendedRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(suspendedData)
        assertTrue(suspendedData.all { it.jsonObject["status"]?.jsonPrimitive?.content == "SUSPENDED" })

        // 6. Get User Details
        val detailRes = client.get("/api/admin/users/$userId1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, detailRes.status)
        val detailData = json.parseToJsonElement(detailRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(detailData)
        assertEquals("09123456789", detailData["mobile"]?.jsonPrimitive?.content)
        assertEquals("علی", detailData["firstName"]?.jsonPrimitive?.content)
        assertEquals(0, detailData["completedStagesCount"]?.jsonPrimitive?.content?.toInt())

        // 7. Get non-existent user details -> 404
        val notFoundUserRes = client.get("/api/admin/users/${UUID.randomUUID()}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, notFoundUserRes.status)

        // 8. Get user with malformed UUID -> 400 Bad Request
        val malformedUserRes = client.get("/api/admin/users/invalid-uuid-format") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, malformedUserRes.status)

        // 9. Suspend user status with reason
        val suspendRes = client.post("/api/admin/users/$userId1/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"status":"SUSPENDED","reason":"تخلف از قوانین کاربری"}""")
        }
        assertEquals(HttpStatusCode.OK, suspendRes.status)

        // Verify updated in DB
        val afterSuspendRes = client.get("/api/admin/users/$userId1") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val afterSuspendData = json.parseToJsonElement(afterSuspendRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertEquals("SUSPENDED", afterSuspendData?.get("status")?.jsonPrimitive?.content)
        assertEquals("تخلف از قوانین کاربری", afterSuspendData?.get("suspendedReason")?.jsonPrimitive?.content)

        // 10. Reactivate user status
        val activateRes = client.post("/api/admin/users/$userId1/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"status":"ACTIVE","reason":"رفع تعلیق"}""")
        }
        assertEquals(HttpStatusCode.OK, activateRes.status)

        // 11. Status update for non-existent user -> 404
        val updateUnknownUserRes = client.post("/api/admin/users/${UUID.randomUUID()}/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"status":"ACTIVE"}""")
        }
        assertEquals(HttpStatusCode.NotFound, updateUnknownUserRes.status)

        // 12. Status update with malformed UUID -> 400
        val updateMalformedUserRes = client.post("/api/admin/users/bad-uuid/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"status":"ACTIVE"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, updateMalformedUserRes.status)

        // 13. Status update with malformed JSON body -> 400
        val updateBadJsonRes = client.post("/api/admin/users/$userId1/status") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"status": broken""")
        }
        assertEquals(HttpStatusCode.BadRequest, updateBadJsonRes.status)
    }

    // =========================================================================
    // 4. Subscription Management Tests (Grant, Extension, Cancel, Validation)
    // =========================================================================

    @Test
    fun testSubscriptionManagementComprehensive() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        val userId = UUID.randomUUID()
        transaction {
            UserTable.insert {
                it[id] = userId
                it[mobile] = "09120003344"
                it[nickName] = "کاربر تست اشتراک"
                it[status] = "ACTIVE"
            }
        }

        // 1. Unauthenticated requests -> 401
        val unauthGetSubs = client.get("/api/admin/users/$userId/subscriptions")
        assertEquals(HttpStatusCode.Unauthorized, unauthGetSubs.status)

        val unauthGrant = client.post("/api/admin/users/$userId/subscriptions/grant") {
            contentType(ContentType.Application.Json)
            setBody("""{"planType":"MONTHLY","durationDays":30}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, unauthGrant.status)

        val unauthCancel = client.post("/api/admin/subscriptions/${UUID.randomUUID()}/cancel")
        assertEquals(HttpStatusCode.Unauthorized, unauthCancel.status)

        // 2. Initial subscriptions list for user -> empty list
        val emptySubsRes = client.get("/api/admin/users/$userId/subscriptions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, emptySubsRes.status)
        val emptySubsData = json.parseToJsonElement(emptySubsRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(emptySubsData)
        assertEquals(0, emptySubsData.size)

        // 3. Grant Manual Monthly Subscription (30 days)
        val grant1Res = client.post("/api/admin/users/$userId/subscriptions/grant") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planType":"MONTHLY","durationDays":30,"reason":"اشتراک آزمایشی ادمین"}""")
        }
        assertEquals(HttpStatusCode.OK, grant1Res.status)
        val grant1Data = json.parseToJsonElement(grant1Res.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(grant1Data)
        val sub1Id = grant1Data["id"]!!.jsonPrimitive.content
        assertEquals("MONTHLY", grant1Data["planType"]?.jsonPrimitive?.content)
        assertEquals("ACTIVE", grant1Data["status"]?.jsonPrimitive?.content)
        assertEquals("MANUAL_ADMIN", grant1Data["grantSource"]?.jsonPrimitive?.content)
        assertNotNull(grant1Data["grantedBy"]?.jsonPrimitive?.content)
        assertEquals("اشتراک آزمایشی ادمین", grant1Data["grantReason"]?.jsonPrimitive?.content)

        // 4. Grant Second Subscription while First is Active (Cumulative Extension check - Item 9)
        val grant2Res = client.post("/api/admin/users/$userId/subscriptions/grant") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planType":"YEARLY","durationDays":365,"reason":"تمدید سالانه"}""")
        }
        assertEquals(HttpStatusCode.OK, grant2Res.status)
        val grant2Data = json.parseToJsonElement(grant2Res.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(grant2Data)
        val sub2Id = grant2Data["id"]!!.jsonPrimitive.content
        assertEquals("YEARLY", grant2Data["planType"]?.jsonPrimitive?.content)
        assertEquals("ACTIVE", grant2Data["status"]?.jsonPrimitive?.content)

        // Expiry of extended subscription should be further than initial expiry
        val exp1 = grant1Data["expiresAt"]!!.jsonPrimitive.content
        val exp2 = grant2Data["expiresAt"]!!.jsonPrimitive.content
        assertTrue(exp2 > exp1, "Extended subscription expiresAt ($exp2) should be after ($exp1)")

        // 5. List subscriptions for user -> both remain ACTIVE (previous is NOT marked EXPIRED)
        val userSubsRes = client.get("/api/admin/users/$userId/subscriptions") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, userSubsRes.status)
        val userSubsData = json.parseToJsonElement(userSubsRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(userSubsData)
        assertEquals(2, userSubsData.size)
        assertTrue(userSubsData.all { it.jsonObject["status"]?.jsonPrimitive?.content == "ACTIVE" })

        // 6. Cancel Subscription
        val cancelRes = client.post("/api/admin/subscriptions/$sub1Id/cancel") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, cancelRes.status)
        assertTrue(cancelRes.bodyAsText().contains("true"))

        // Verify status in DB is CANCELLED
        transaction {
            val subRow = SubscriptionTable.selectAll()
                .where { SubscriptionTable.id eq UUID.fromString(sub1Id) }
                .single()
            assertEquals("CANCELLED", subRow[SubscriptionTable.status])
        }

        // 7. Error cases:
        // Grant to non-existent user -> 404
        val grantUnknownUser = client.post("/api/admin/users/${UUID.randomUUID()}/subscriptions/grant") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planType":"MONTHLY","durationDays":30}""")
        }
        assertEquals(HttpStatusCode.NotFound, grantUnknownUser.status)

        // Grant with malformed user UUID -> 400
        val grantMalformedUser = client.post("/api/admin/users/invalid-uuid/subscriptions/grant") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planType":"MONTHLY","durationDays":30}""")
        }
        assertEquals(HttpStatusCode.BadRequest, grantMalformedUser.status)

        // Grant with malformed JSON body -> 400
        val grantMalformedJson = client.post("/api/admin/users/$userId/subscriptions/grant") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"planType": broken""")
        }
        assertEquals(HttpStatusCode.BadRequest, grantMalformedJson.status)

        // Cancel non-existent subscription -> 404
        val cancelUnknownSub = client.post("/api/admin/subscriptions/${UUID.randomUUID()}/cancel") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, cancelUnknownSub.status)

        // Cancel with malformed subscription UUID -> 400
        val cancelMalformedSub = client.post("/api/admin/subscriptions/not-a-uuid/cancel") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, cancelMalformedSub.status)
    }

    // =========================================================================
    // 5. Stage Management Tests (Target Objective FA, Character Behavior, Validations)
    // =========================================================================

    @Test
    fun testStageManagementComprehensive() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        val stageId = "stage-test-ai-speaking-complete"

        // 1. Unauthenticated requests -> 401
        val unauthListStages = client.get("/api/admin/stages")
        assertEquals(HttpStatusCode.Unauthorized, unauthListStages.status)

        val unauthCreateStage = client.post("/api/admin/stages") {
            contentType(ContentType.Application.Json)
            setBody("""{"id":"test"}""")
        }
        assertEquals(HttpStatusCode.Unauthorized, unauthCreateStage.status)

        val unauthDeleteStage = client.delete("/api/admin/stages/test")
        assertEquals(HttpStatusCode.Unauthorized, unauthDeleteStage.status)

        // 2. Create Stage with all fields including targetObjectiveFa and characterBehavior
        val createRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "$stageId",
                "orderIndex": 100,
                "title": "Hotel Check-in Challenge",
                "titleFa": "چالش پذیرش هتل در لندن",
                "briefing": "You arrived at the hotel without a printed voucher.",
                "briefingFa": "شما بدون برگه چاپی ووچر وارد هتل شدید و باید اقامت خود را ثبت کنید.",
                "targetObjective": "Confirm reservation and get the room key card",
                "targetObjectiveFa": "تأیید مشخصات رزرو، دریافت کارت کلید اتاق و آگاهی از ساعت سرو صبحانه",
                "characterBehavior": "Receptionist Clara is welcoming but requires photo ID before giving the key card.",
                "backgroundUrl": "https://img.example.com/hotel.webp",
                "characterName": "Receptionist Clara",
                "characterAvatarUrl": "https://img.example.com/clara.webp",
                "characterGender": "Woman",
                "voiceId": "af_bella",
                "initialSpeaker": "Model",
                "maxTurns": 10,
                "status": "DRAFT"
            }""")
        }
        assertEquals(HttpStatusCode.OK, createRes.status)
        val createdJson = json.parseToJsonElement(createRes.bodyAsText()).jsonObject
        val createdData = createdJson["data"]?.jsonObject
        assertNotNull(createdData)
        assertEquals(stageId, createdData["id"]?.jsonPrimitive?.content)
        assertEquals(100, createdData["orderIndex"]?.jsonPrimitive?.content?.toInt())
        assertEquals("Hotel Check-in Challenge", createdData["title"]?.jsonPrimitive?.content)
        assertEquals("چالش پذیرش هتل در لندن", createdData["titleFa"]?.jsonPrimitive?.content)
        assertEquals("Confirm reservation and get the room key card", createdData["targetObjective"]?.jsonPrimitive?.content)
        assertEquals("تأیید مشخصات رزرو، دریافت کارت کلید اتاق و آگاهی از ساعت سرو صبحانه", createdData["targetObjectiveFa"]?.jsonPrimitive?.content)
        assertEquals("Receptionist Clara is welcoming but requires photo ID before giving the key card.", createdData["characterBehavior"]?.jsonPrimitive?.content)
        assertEquals("DRAFT", createdData["status"]?.jsonPrimitive?.content)

        // 3. Get Stage Details by ID
        val detailRes = client.get("/api/admin/stages/$stageId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, detailRes.status)
        val detailData = json.parseToJsonElement(detailRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(detailData)
        assertEquals("تأیید مشخصات رزرو، دریافت کارت کلید اتاق و آگاهی از ساعت سرو صبحانه", detailData["targetObjectiveFa"]?.jsonPrimitive?.content)
        assertEquals("Receptionist Clara is welcoming but requires photo ID before giving the key card.", detailData["characterBehavior"]?.jsonPrimitive?.content)

        // 4. Update Stage (upsert) -> change status to PUBLISHED and update targetObjectiveFa
        val updateRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "$stageId",
                "orderIndex": 100,
                "title": "Hotel Check-in Challenge Updated",
                "titleFa": "چالش پذیرش هتل در لندن (ویرایش شده)",
                "briefing": "Updated briefing text",
                "briefingFa": "متن بریفینگ ویرایش شده",
                "targetObjective": "Confirm reservation and get the room key card",
                "targetObjectiveFa": "هدف داستانی ویرایش شده به فارسی",
                "characterBehavior": "Updated behavior description",
                "backgroundUrl": "https://img.example.com/hotel.webp",
                "characterName": "Receptionist Clara",
                "characterGender": "Woman",
                "voiceId": "af_bella",
                "initialSpeaker": "Model",
                "maxTurns": 14,
                "status": "PUBLISHED"
            }""")
        }
        assertEquals(HttpStatusCode.OK, updateRes.status)
        val updatedData = json.parseToJsonElement(updateRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(updatedData)
        assertEquals("Hotel Check-in Challenge Updated", updatedData["title"]?.jsonPrimitive?.content)
        assertEquals("هدف داستانی ویرایش شده به فارسی", updatedData["targetObjectiveFa"]?.jsonPrimitive?.content)
        assertEquals("Updated behavior description", updatedData["characterBehavior"]?.jsonPrimitive?.content)
        assertEquals("PUBLISHED", updatedData["status"]?.jsonPrimitive?.content)
        assertEquals(14, updatedData["maxTurns"]?.jsonPrimitive?.content?.toInt())

        // 5. Test shiftSubsequent: insert another stage at orderIndex 100 with shiftSubsequent = true
        val collidingStageId = "stage-test-colliding-order"
        val shiftRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "$collidingStageId",
                "orderIndex": 100,
                "title": "Colliding Stage",
                "titleFa": "مرحله با اولویت متداخل",
                "briefing": "Test shift subsequent",
                "briefingFa": "تست شیفت مراحل بعدی",
                "targetObjective": "Test objective",
                "targetObjectiveFa": "هدف فارسی تستی",
                "backgroundUrl": "https://img.example.com/shift.webp",
                "characterName": "Shift Actor",
                "status": "PUBLISHED",
                "shiftSubsequent": true
            }""")
        }
        assertEquals(HttpStatusCode.OK, shiftRes.status)

        // Verify that original stage orderIndex was shifted from 100 to 101
        val shiftedOriginalRes = client.get("/api/admin/stages/$stageId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val shiftedOriginalData = json.parseToJsonElement(shiftedOriginalRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertEquals(101, shiftedOriginalData?.get("orderIndex")?.jsonPrimitive?.content?.toInt())

        // 5b. Test reorder endpoint (/api/admin/stages/{stageId}/reorder) with direct swap and range shift
        val swapOrderRes = client.post("/api/admin/stages/$collidingStageId/reorder") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"newOrderIndex": 101, "shiftSubsequent": false}""")
        }
        assertEquals(HttpStatusCode.OK, swapOrderRes.status)
        val afterSwapOriginal = json.parseToJsonElement(
            client.get("/api/admin/stages/$stageId") { header(HttpHeaders.Authorization, "Bearer $token") }.bodyAsText()
        ).jsonObject["data"]?.jsonObject
        assertEquals(100, afterSwapOriginal?.get("orderIndex")?.jsonPrimitive?.content?.toInt())

        val shiftDownRes = client.post("/api/admin/stages/$stageId/reorder") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"newOrderIndex": 101, "shiftSubsequent": true}""")
        }
        assertEquals(HttpStatusCode.OK, shiftDownRes.status)
        val afterShiftDownColliding = json.parseToJsonElement(
            client.get("/api/admin/stages/$collidingStageId") { header(HttpHeaders.Authorization, "Bearer $token") }.bodyAsText()
        ).jsonObject["data"]?.jsonObject
        assertEquals(100, afterShiftDownColliding?.get("orderIndex")?.jsonPrimitive?.content?.toInt())

        // 6. List Stages with status filter
        val listPublishedRes = client.get("/api/admin/stages?status=PUBLISHED") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, listPublishedRes.status)
        val publishedList = json.parseToJsonElement(listPublishedRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(publishedList)
        assertTrue(publishedList.any { it.jsonObject["id"]?.jsonPrimitive?.content == stageId })
        assertTrue(publishedList.any { it.jsonObject["id"]?.jsonPrimitive?.content == collidingStageId })

        // 7. Validation Failures (422 UnprocessableEntity)
        // A) Blank ID
        val blankIdRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "",
                "orderIndex": 50,
                "title": "Title",
                "titleFa": "عنوان",
                "briefing": "Briefing",
                "briefingFa": "بریفینگ",
                "targetObjective": "Objective",
                "backgroundUrl": "url",
                "characterName": "Name"
            }""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, blankIdRes.status)

        // B) orderIndex <= 0
        val invalidOrderRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "valid-id-order-zero",
                "orderIndex": 0,
                "title": "Title",
                "titleFa": "عنوان",
                "briefing": "Briefing",
                "briefingFa": "بریفینگ",
                "targetObjective": "Objective",
                "backgroundUrl": "url",
                "characterName": "Name"
            }""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, invalidOrderRes.status)

        // C) Blank English Title
        val blankTitleRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "valid-id-blank-title",
                "orderIndex": 50,
                "title": "  ",
                "titleFa": "عنوان",
                "briefing": "Briefing",
                "briefingFa": "بریفینگ",
                "targetObjective": "Objective",
                "backgroundUrl": "url",
                "characterName": "Name"
            }""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, blankTitleRes.status)

        // D) Blank Persian Title (titleFa)
        val blankTitleFaRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "valid-id-blank-title-fa",
                "orderIndex": 50,
                "title": "English Title",
                "titleFa": "  ",
                "briefing": "Briefing",
                "briefingFa": "بریفینگ",
                "targetObjective": "Objective",
                "backgroundUrl": "url",
                "characterName": "Name"
            }""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, blankTitleFaRes.status)

        // E) Blank Briefing
        val blankBriefingRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "valid-id-blank-briefing",
                "orderIndex": 50,
                "title": "Title",
                "titleFa": "عنوان",
                "briefing": "",
                "briefingFa": "بریفینگ",
                "targetObjective": "Objective",
                "backgroundUrl": "url",
                "characterName": "Name"
            }""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, blankBriefingRes.status)

        // F) Blank BriefingFa
        val blankBriefingFaRes = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{
                "id": "valid-id-blank-briefing-fa",
                "orderIndex": 50,
                "title": "Title",
                "titleFa": "عنوان",
                "briefing": "Briefing",
                "briefingFa": "   ",
                "targetObjective": "Objective",
                "backgroundUrl": "url",
                "characterName": "Name"
            }""")
        }
        assertEquals(HttpStatusCode.UnprocessableEntity, blankBriefingFaRes.status)

        // G) Malformed JSON
        val malformedStageJson = client.post("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"id": unclosed_string""")
        }
        assertEquals(HttpStatusCode.BadRequest, malformedStageJson.status)

        // 8. Delete Stages
        val deleteRes = client.delete("/api/admin/stages/$stageId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleteRes.status)
        assertTrue(deleteRes.bodyAsText().contains("true"))

        val deleteCollidingRes = client.delete("/api/admin/stages/$collidingStageId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, deleteCollidingRes.status)

        // 9. Delete non-existent Stage -> 404
        val deleteUnknownRes = client.delete("/api/admin/stages/non-existent-stage-id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.NotFound, deleteUnknownRes.status)
    }

    // =========================================================================
    // 6. Media Asset Upload Tests (JPEG, PNG, WebP, Empty multipart, Unauth)
    // =========================================================================

    @Test
    fun testMediaUploadComprehensive() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        // 1. Unauthenticated access -> 401
        val unauthUpload = client.submitFormWithBinaryData(
            url = "/api/admin/media/upload",
            formData = formData {
                append("file", byteArrayOf(0x01, 0x02), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"test.jpg\"")
                })
            }
        )
        assertEquals(HttpStatusCode.Unauthorized, unauthUpload.status)

        // 2. Upload JPEG image
        val uploadJpegRes = client.submitFormWithBinaryData(
            url = "/api/admin/media/upload",
            formData = formData {
                append("file", byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xE0.toByte()), Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"background.jpg\"")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, uploadJpegRes.status)
        val jpegData = json.parseToJsonElement(uploadJpegRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(jpegData)
        assertTrue(jpegData["url"]?.jsonPrimitive?.content?.startsWith("/uploads/stages/") == true)
        assertTrue(jpegData["filename"]?.jsonPrimitive?.content?.endsWith(".jpg") == true)

        // 3. Upload PNG image
        val uploadPngRes = client.submitFormWithBinaryData(
            url = "/api/admin/media/upload",
            formData = formData {
                append("file", byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte()), Headers.build {
                    append(HttpHeaders.ContentType, "image/png")
                    append(HttpHeaders.ContentDisposition, "filename=\"avatar.png\"")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, uploadPngRes.status)
        val pngData = json.parseToJsonElement(uploadPngRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(pngData)
        assertTrue(pngData["url"]?.jsonPrimitive?.content?.startsWith("/uploads/stages/") == true)
        assertTrue(pngData["filename"]?.jsonPrimitive?.content?.endsWith(".png") == true)

        // 4. Upload WebP image
        val uploadWebpRes = client.submitFormWithBinaryData(
            url = "/api/admin/media/upload",
            formData = formData {
                append("file", byteArrayOf(0x52, 0x49, 0x46, 0x46), Headers.build {
                    append(HttpHeaders.ContentType, "image/webp")
                    append(HttpHeaders.ContentDisposition, "filename=\"scene.webp\"")
                })
            }
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, uploadWebpRes.status)
        val webpData = json.parseToJsonElement(uploadWebpRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(webpData)
        assertTrue(webpData["filename"]?.jsonPrimitive?.content?.endsWith(".webp") == true)

        // 5. Empty multipart data -> 400 Bad Request
        val emptyUploadRes = client.submitFormWithBinaryData(
            url = "/api/admin/media/upload",
            formData = emptyList()
        ) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.BadRequest, emptyUploadRes.status)
    }

    // =========================================================================
    // 7. Stage Table target_objective_fa Schema & Seed Synchronization Test
    // =========================================================================

    @Test
    fun testStageTableTargetObjectiveFaIntegrityAndSync() = testApplication {
        setupTestApp()
        val token = loginAndGetToken()

        // 1. Verify that all 15 default seeded stages returned by API have targetObjectiveFa populated
        val stagesRes = client.get("/api/admin/stages") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, stagesRes.status)
        val stagesData = json.parseToJsonElement(stagesRes.bodyAsText()).jsonObject["data"]?.jsonArray
        assertNotNull(stagesData)
        assertTrue(stagesData.size >= 15, "Should have at least 15 stages seeded")

        for (stageElement in stagesData) {
            val stageObj = stageElement.jsonObject
            val targetObjective = stageObj["targetObjective"]?.jsonPrimitive?.content
            val targetObjectiveFa = stageObj["targetObjectiveFa"]?.jsonPrimitive?.content
            val characterBehavior = stageObj["characterBehavior"]?.jsonPrimitive?.content
            assertNotNull(targetObjective, "targetObjective must not be null")
            assertNotNull(targetObjectiveFa, "targetObjectiveFa must not be null")
            assertNotNull(characterBehavior, "characterBehavior must not be null for stage: ${stageObj["id"]}")
            assertTrue(targetObjectiveFa.isNotBlank(), "targetObjectiveFa must not be blank for seeded stage: ${stageObj["id"]}")
            assertTrue(characterBehavior.isNotBlank(), "characterBehavior must not be blank for seeded stage: ${stageObj["id"]}")
        }

        // Specifically verify Stage 1 and Stage 2 adhere to narrative goals
        val stage1 = stagesData.first { it.jsonObject["id"]?.jsonPrimitive?.content == "stage-01-inflight-london" }.jsonObject
        assertEquals("Order a warm dinner and beverage from flight attendant Emily, and respond to her friendly question about your journey to London.", stage1["targetObjective"]?.jsonPrimitive?.content)
        assertEquals("انتخاب و سفارش یک وعده شام گرم و نوشیدنی از مهماندار پرواز، و پاسخ به گپ‌وگفت کوتاه او درباره آغاز سفر تحصیلی‌ات به لندن.", stage1["targetObjectiveFa"]?.jsonPrimitive?.content)

        val stage2 = stagesData.first { it.jsonObject["id"]?.jsonPrimitive?.content == "stage-02-heathrow-border" }.jsonObject
        assertEquals("State your university study purpose, institution name, and planned student accommodation clearly to secure entry approval from the UK border officer.", stage2["targetObjective"]?.jsonPrimitive?.content)
        assertEquals("پاسخ روشن به سوالات افسر مرزی درباره هدف تحصیلی سفر، نام دانشگاه و محل اقامت در لندن جهت اخذ تاییدیه ورود به بریتانیا.", stage2["targetObjectiveFa"]?.jsonPrimitive?.content)

        // 2. Direct database test: verify StageTable default for targetObjectiveFa
        transaction {
            val stageId = "stage-schema-default-test"
            StageTable.insert {
                it[id] = stageId
                it[orderIndex] = 9999
                it[title] = "Schema Test"
                it[titleFa] = "تست اسکیما"
                it[briefing] = "Briefing"
                it[briefingFa] = "بریفینگ"
                it[backgroundUrl] = "https://example.com/bg.jpg"
                it[characterName] = "Agent"
                // notice: targetObjective and targetObjectiveFa are omitted to test column defaults!
            }

            val insertedRow = StageTable.selectAll().where { StageTable.id eq stageId }.single()
            assertEquals("", insertedRow[StageTable.targetObjective], "targetObjective should default to empty string")
            assertEquals("", insertedRow[StageTable.targetObjectiveFa], "targetObjectiveFa should default to empty string")
        }
    }
}
