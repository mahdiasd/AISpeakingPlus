package ir.speaking.feature.admin.subscription.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.admin.model.AdminSubscriptionGrantRequest
import ir.speaking.feature.admin.model.AdminSubscriptionItemDto
import ir.speaking.feature.admin.subscription.service.AdminSubscriptionService
import java.util.UUID

@OptIn(ExperimentalKtorApi::class)
fun Route.adminSubscriptionRouting(
    adminSubscriptionService: AdminSubscriptionService,
    auditLogService: AuditLogService
) {
    route("/api/admin") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            get("/users/{userId}/subscriptions") {
                val idStr = call.parameters["userId"] ?: return@get call.failureRespond(HttpStatusCode.BadRequest, "شناسه کاربر الزامی است")
                val userId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@get call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه کاربر نامعتبر است")
                }

                val subs = adminSubscriptionService.getUserSubscriptions(userId)
                call.successRespond(subs, message = "سوابق اشتراک کاربر با موفقیت دریافت شد")
            }.describe {
                tag("Admin Subscriptions")
                summary = "Get User Subscriptions"
                description = "Get subscription history and active subscription for a specific user"
                parameters {
                    path("userId") { description = "User UUID" }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User subscriptions list"
                        schema = jsonSchema<SuccessResponse<List<AdminSubscriptionItemDto>>>()
                    }
                }
            }

            post("/users/{userId}/subscriptions/grant") {
                val principal = call.principal<AdminPrincipal>()
                val idStr = call.parameters["userId"] ?: return@post call.failureRespond(HttpStatusCode.BadRequest, "شناسه کاربر الزامی است")
                val userId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@post call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه کاربر نامعتبر است")
                }

                val request = try {
                    call.receive<AdminSubscriptionGrantRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست اعطای اشتراک نامعتبر است")
                    return@post
                }

                val granted = try {
                    adminSubscriptionService.grantSubscription(userId, principal?.id, request)
                } catch (e: NoSuchElementException) {
                    call.failureRespond(HttpStatusCode.NotFound, e.message ?: "کاربر یافت نشد")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "SUBSCRIPTION_GRANT_MANUAL",
                    targetType = "USER",
                    targetId = userId.toString(),
                    detailsJson = """{"planType":"${granted.planType}","durationDays":${request.durationDays},"expiresAt":"${granted.expiresAt}","reason":"${request.reason ?: ""}"}"""
                )

                call.successRespond(granted, message = "اشتراک با موفقیت اعطا شد")
            }.describe {
                tag("Admin Subscriptions")
                summary = "Grant Manual Subscription"
                description = "Manually grant a subscription without payment flow, attributed to admin"
                parameters {
                    path("userId") { description = "User UUID" }
                }
                requestBody {
                    description = "Subscription grant details"
                    schema = jsonSchema<AdminSubscriptionGrantRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Subscription granted"
                        schema = jsonSchema<SuccessResponse<AdminSubscriptionItemDto>>()
                    }
                }
            }

            post("/subscriptions/{id}/cancel") {
                val principal = call.principal<AdminPrincipal>()
                val idStr = call.parameters["id"] ?: return@post call.failureRespond(HttpStatusCode.BadRequest, "شناسه اشتراک الزامی است")
                val subId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@post call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه اشتراک نامعتبر است")
                }

                val cancelled = adminSubscriptionService.cancelSubscription(subId)
                if (!cancelled) {
                    call.failureRespond(HttpStatusCode.NotFound, "اشتراک یافت نشد")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "SUBSCRIPTION_CANCEL",
                    targetType = "SUBSCRIPTION",
                    targetId = subId.toString(),
                    detailsJson = """{"subscriptionId":"$subId"}"""
                )

                call.successRespond(mapOf("cancelled" to true), message = "اشتراک با موفقیت لغو شد")
            }.describe {
                tag("Admin Subscriptions")
                summary = "Cancel Subscription"
                description = "Cancel an active subscription"
                parameters {
                    path("id") { description = "Subscription UUID" }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Subscription cancelled"
                        schema = jsonSchema<SuccessResponse<Map<String, Boolean>>>()
                    }
                }
            }
        }
    }
}
