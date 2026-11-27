package ir.speaking.feature.config

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import ir.speaking.core.testModule
import ir.speaking.feature.admin.db.AdminAuditTable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConfigRoutingTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun testV1ConfigPostReturnsForceUpdate() = testApplication {
        application {
            testModule(dropTables = false)
        }

        val response = client.post("/api/v1/config") {
            contentType(ContentType.Application.Json)
            setBody("""{"deviceName":"Pixel 7","androidVersion":"14","firebaseToken":"sample_fcm_token_123"}""")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val parsed = json.parseToJsonElement(body).jsonObject
        val data = parsed["data"]?.jsonObject
        assertNotNull(data, "Expected data object in response")

        val update = data["update"]?.jsonObject
        assertNotNull(update, "Expected update object in data")

        val forceVersion = update["force_version"]?.jsonPrimitive?.content?.toIntOrNull()
        assertTrue((forceVersion ?: 0) >= 10, "force_version should be >= 10 to force update over v9")

        val link = update["link"]?.jsonPrimitive?.content
        assertNotNull(link)
        assertTrue(link.contains("cafebazaar"), "Link should direct to Cafe Bazaar")
    }

    @Test
    fun testV1ConfigGetReturnsValidConfig() = testApplication {
        application {
            testModule(dropTables = false)
        }

        val response = client.get("/api/v1/config")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val parsed = json.parseToJsonElement(body).jsonObject
        val data = parsed["data"]?.jsonObject
        assertNotNull(data)
        val tokenAlive = data["tokenAlive"]?.jsonPrimitive?.content?.toBooleanStrictOrNull()
        assertEquals(true, tokenAlive)
    }

    @Test
    fun testV2ConfigReturnsDynamicValuesAfterAdminUpdate() = testApplication {
        application {
            testModule(dropTables = true)
        }

        // 1. Unauthenticated admin config access must be rejected
        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/admin/config/app-version").status)
        assertEquals(
            HttpStatusCode.Unauthorized,
            client.put("/api/admin/config/app-version") {
                contentType(ContentType.Application.Json)
                setBody("""{"lastVersion":11,"minVersion":10,"suggestVersion":11,"link":"https://cafebazaar.ir/app/ir.aispeaking","message":"msg"}""")
            }.status
        )

        // 2. Login as seeded SuperAdmin
        val loginRes = client.post("/api/admin/auth/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"admin@aispeaking.ir","password":"Admin@123456!"}""")
        }
        assertEquals(HttpStatusCode.OK, loginRes.status)
        val token = json.parseToJsonElement(loginRes.bodyAsText())
            .jsonObject["data"]?.jsonObject?.get("token")?.jsonPrimitive?.content
        assertNotNull(token)

        // 3. Initial GET returns safe defaults
        val initialAdminGet = client.get("/api/admin/config/app-version") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, initialAdminGet.status)
        val initialData = json.parseToJsonElement(initialAdminGet.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(initialData)
        assertEquals(10, initialData["lastVersion"]?.jsonPrimitive?.intOrNull)
        assertEquals(10, initialData["minVersion"]?.jsonPrimitive?.intOrNull)
        assertEquals(10, initialData["suggestVersion"]?.jsonPrimitive?.intOrNull)
        assertEquals("https://cafebazaar.ir/app/ir.aispeaking", initialData["link"]?.jsonPrimitive?.content)

        // 4. Invalid updates return 400 Bad Request
        val invalidVersionRes = client.put("/api/admin/config/app-version") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"lastVersion":10,"minVersion":12,"suggestVersion":10,"link":"https://cafebazaar.ir/app/ir.aispeaking","message":"پیام"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidVersionRes.status)

        val invalidLinkRes = client.put("/api/admin/config/app-version") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"lastVersion":12,"minVersion":10,"suggestVersion":11,"link":"invalid-url","message":"پیام"}""")
        }
        assertEquals(HttpStatusCode.BadRequest, invalidLinkRes.status)

        val blankMessageRes = client.put("/api/admin/config/app-version") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody("""{"lastVersion":12,"minVersion":10,"suggestVersion":11,"link":"https://cafebazaar.ir/app/ir.aispeaking","message":"  "}""")
        }
        assertEquals(HttpStatusCode.BadRequest, blankMessageRes.status)

        // 5. Valid PUT updates config in DB
        val newLink = "https://cafebazaar.ir/app/ir.aispeaking?ref=update"
        val newMessage = "نسخه ۱۵ با امکانات جدید منتشر شد. لطفاً از کافه‌بازار به‌روزرسانی کنید."
        val updateRes = client.put("/api/admin/config/app-version") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(
                """{
                    "lastVersion": 15,
                    "minVersion": 12,
                    "suggestVersion": 14,
                    "link": "$newLink",
                    "message": "$newMessage"
                }"""
            )
        }
        assertEquals(HttpStatusCode.OK, updateRes.status)
        val updatedAdminData = json.parseToJsonElement(updateRes.bodyAsText()).jsonObject["data"]?.jsonObject
        assertNotNull(updatedAdminData)
        assertEquals(15, updatedAdminData["lastVersion"]?.jsonPrimitive?.intOrNull)
        assertEquals(12, updatedAdminData["minVersion"]?.jsonPrimitive?.intOrNull)
        assertEquals(14, updatedAdminData["suggestVersion"]?.jsonPrimitive?.intOrNull)
        assertEquals(newLink, updatedAdminData["link"]?.jsonPrimitive?.content)
        assertEquals(newMessage, updatedAdminData["message"]?.jsonPrimitive?.content)

        // 6. Public v1 & v2 GET/POST endpoints reflect updated values
        val publicRequests = listOf(
            client.get("/api/v1/config"),
            client.post("/api/v1/config") {
                contentType(ContentType.Application.Json)
                setBody("""{"deviceName":"Pixel 8","versionCode":11}""")
            },
            client.get("/api/v2/config"),
            client.post("/api/v2/config") {
                contentType(ContentType.Application.Json)
                setBody("""{"deviceName":"Pixel 8","versionCode":11}""")
            }
        )

        for (res in publicRequests) {
            assertEquals(HttpStatusCode.OK, res.status)
            val updateObj = json.parseToJsonElement(res.bodyAsText())
                .jsonObject["data"]?.jsonObject?.get("update")?.jsonObject
            assertNotNull(updateObj)
            assertEquals(12, updateObj["force_version"]?.jsonPrimitive?.intOrNull)
            assertEquals(15, updateObj["last_version"]?.jsonPrimitive?.intOrNull)
            assertEquals(14, updateObj["suggest_version"]?.jsonPrimitive?.intOrNull)
            assertEquals(newLink, updateObj["link"]?.jsonPrimitive?.content)
            assertEquals(newMessage, updateObj["message"]?.jsonPrimitive?.content)
        }

        // 7. AuditLogService recorded APP_VERSION_CONFIG_UPDATE
        transaction {
            val auditLogs = AdminAuditTable.selectAll()
                .where { AdminAuditTable.action eq "APP_VERSION_CONFIG_UPDATE" }
                .toList()
            assertTrue(auditLogs.isNotEmpty(), "Expected APP_VERSION_CONFIG_UPDATE audit log")
            assertEquals("APP_VERSION_CONFIG", auditLogs.first()[AdminAuditTable.targetType])
            assertEquals("android", auditLogs.first()[AdminAuditTable.targetId])
        }
    }
}
