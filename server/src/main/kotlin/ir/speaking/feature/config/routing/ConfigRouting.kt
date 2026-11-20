package ir.speaking.feature.config.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.getUserUid
import ir.speaking.feature.config.dto.ConfigRequestDto
import ir.speaking.feature.config.dto.ConfigResponseDto
import ir.speaking.feature.config.dto.UpdateResponseDto
import ir.speaking.feature.notification.service.DeviceTokenService
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.util.UUID

@OptIn(ExperimentalKtorApi::class)
fun Application.configRouting() {
    val deviceTokenService by inject<DeviceTokenService>()
    val json = Json { ignoreUnknownKeys = true; isLenient = true }

    routing {
        suspend fun handleConfig(call: RoutingCall) {
            val req = try {
                val text = call.receiveText()
                if (text.isNotBlank()) {
                    json.decodeFromString<ConfigRequestDto>(text)
                } else null
            } catch (_: Exception) {
                null
            }

            var userId: UUID? = null
            try {
                val authHeader = call.request.headers[HttpHeaders.Authorization]
                if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ")) {
                    userId = call.getUserUid()
                }
            } catch (_: Exception) {}

            val fcmToken = req?.firebaseToken?.trim()?.takeIf { it.isNotBlank() }
            if (fcmToken != null) {
                try {
                    deviceTokenService.registerOrUpdateToken(
                        token = fcmToken,
                        userId = userId,
                        platform = "android",
                        topic = "AISpeakingPublic",
                        deviceName = req.deviceName,
                        appVersion = req.appVersion ?: req.versionCode?.toString()
                    )
                } catch (_: Exception) {}
            }

            // Force update response directing legacy users (version <= 9) to update to V2 (version 10+)
            val updateConfig = UpdateResponseDto(
                forceVersion = 10,
                lastVersion = 10,
                suggestVersion = 10,
                link = "https://cafebazaar.ir/app/ir.aispeaking",
                message = "نسخه جدید AI Speaking با مراحل داستانی و هوش مصنوعی پیشرفته منتشر شد. برای ادامه لطفاً برنامه را بروزرسانی نمایید."
            )

            val response = ConfigResponseDto(
                update = updateConfig,
                welcomeMessage = null,
                tokenAlive = true
            )

            call.successRespond(response, message = "تنظیمات با موفقیت دریافت شد")
        }

        // V1 Compatibility Route (Called by legacy clients on Splash)
        route("/api/v1/config") {
            post {
                handleConfig(call)
            }.describe {
                tag("App Config")
                summary = "Get App Config & Force Update (v1)"
                description = "Returns application update requirements and server configuration for legacy and mobile clients"
                requestBody {
                    description = "Optional device info and push token"
                    required = false
                    schema = jsonSchema<ConfigRequestDto>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Configuration data"
                        schema = jsonSchema<SuccessResponse<ConfigResponseDto>>()
                    }
                }
            }

            get {
                handleConfig(call)
            }.describe {
                tag("App Config")
                summary = "Get App Config (v1 GET)"
                description = "Returns application update requirements and server configuration"
            }
        }

        // V2 Route (For modern clients)
        route("/api/v2/config") {
            post {
                handleConfig(call)
            }.describe {
                tag("App Config")
                summary = "Get App Config & Force Update (v2)"
                description = "Returns application update requirements and server configuration for V2 clients"
            }

            get {
                handleConfig(call)
            }.describe {
                tag("App Config")
                summary = "Get App Config (v2 GET)"
                description = "Returns application update requirements and server configuration"
            }
        }
    }
}
