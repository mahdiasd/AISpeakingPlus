package ir.speaking.feature.admin.notification.routing

import com.google.firebase.messaging.FirebaseMessagingException
import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.admin.notification.model.AdminNotificationSendRequest
import ir.speaking.feature.admin.notification.model.AdminNotificationSendResponse
import ir.speaking.feature.admin.notification.model.RegisterTokenRequest
import ir.speaking.feature.admin.notification.model.RegisterTokenResponse
import ir.speaking.feature.admin.notification.service.AdminNotificationService
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("AdminNotificationRouting")

@OptIn(ExperimentalKtorApi::class)
fun Route.adminNotificationRouting(
    adminNotificationService: AdminNotificationService,
    auditLogService: AuditLogService
) {
    // Protected admin notification routes
    route("/api/admin/notifications") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {
            post("/send") {
                val principal = call.principal<AdminPrincipal>()
                val req = try {
                    call.receive<AdminNotificationSendRequest>()
                } catch (e: Exception) {
                    logger.warn("Invalid notification request format: ${e.message}")
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست ارسال نوتیفیکیشن نامعتبر است: ${e.message}")
                    return@post
                }

                if (req.title.isBlank() || req.body.isBlank()) {
                    call.failureRespond(HttpStatusCode.BadRequest, "عنوان و متن پیام نوتیفیکیشن نمی‌توانند خالی باشند")
                    return@post
                }

                try {
                    val res = adminNotificationService.sendNotification(req)

                    try {
                        auditLogService.log(
                            adminId = principal?.id,
                            action = "SEND_NOTIFICATION",
                            targetType = "NOTIFICATION",
                            targetId = res.messageId,
                            detailsJson = buildJsonObject {
                                put("title", req.title)
                                put("body", req.body)
                                put("targetType", req.targetType ?: "topic")
                                put("targetValue", req.targetValue ?: "aispeaking_plus_v1")
                                put("stageId", req.stageId ?: "")
                                put("screen", req.screen ?: "")
                                put("imageUrl", req.imageUrl ?: "")
                                put("url", req.url ?: "")
                            }.toString()
                        )
                    } catch (e: Exception) {
                        logger.warn("Failed to write audit log for notification send", e)
                    }

                    call.successRespond(res, message = "نوتیفیکیشن با موفقیت به فایربیس ارسال شد")
                } catch (e: FirebaseMessagingException) {
                    logger.error("FirebaseMessagingException while sending notification: errorCode=${e.errorCode}, message=${e.message}", e)
                    call.failureRespond(
                        HttpStatusCode.BadGateway,
                        "خطای فایربیس هنگام ارسال [${e.errorCode}]: ${e.message ?: "پاسخ ناموفق از سرور فایربیس"}"
                    )
                } catch (e: IllegalArgumentException) {
                    logger.warn("Validation error sending notification: ${e.message}")
                    call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "پارامترهای ارسالی نامعتبر است")
                } catch (e: Exception) {
                    logger.error("Unexpected error sending notification: ${e.message}", e)
                    call.failureRespond(
                        HttpStatusCode.InternalServerError,
                        "خطای سرور در ارسال نوتیفیکیشن: ${e.message ?: e::class.simpleName}"
                    )
                }
            }.describe {
                tag("Admin Notifications")
                summary = "Send Push Notification"
                description = "Send FCM push notification to topic or device token with deep link data"
                responses {
                    HttpStatusCode.OK {
                        description = "Notification sent response"
                        schema = jsonSchema<SuccessResponse<AdminNotificationSendResponse>>()
                    }
                }
            }
        }
    }

    // Public / client token registration route to subscribe tokens to topics
    route("/api/v1/notifications") {
        post("/register-token") {
            val req = try {
                call.receive<RegisterTokenRequest>()
            } catch (e: Exception) {
                call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست نامعتبر است")
                return@post
            }

            if (req.token.isBlank()) {
                call.failureRespond(HttpStatusCode.BadRequest, "توکن الزامی است")
                return@post
            }

            try {
                val topic = req.topic?.trim()?.ifBlank { null } ?: "aispeaking_plus_v1"
                adminNotificationService.subscribeTokenToTopic(req.token.trim(), topic)
                call.successRespond(
                    RegisterTokenResponse(success = true, message = "توکن با موفقیت در کانال $topic ثبت شد"),
                    message = "توکن با موفقیت در کانال ثبت شد"
                )
            } catch (e: Exception) {
                logger.warn("Failed to subscribe token to topic: ${e.message}")
                call.successRespond(
                    RegisterTokenResponse(success = false, message = "سرویس اعلان موقتاً غیرفعال است: ${e.message}"),
                    message = "سرویس اعلان در دسترس نیست"
                )
            }
        }.describe {
            tag("Client Notifications")
            summary = "Register FCM token"
            description = "Subscribes client device or web token to a notification topic"
        }
    }
}
