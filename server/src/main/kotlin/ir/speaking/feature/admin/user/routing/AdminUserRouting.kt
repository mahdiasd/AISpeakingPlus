package ir.speaking.feature.admin.user.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.FailureResponse
import ir.speaking.core.response.PagingMeta
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.failureRespond
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.auth.AdminPrincipal
import ir.speaking.feature.admin.model.AdminUserDetailDto
import ir.speaking.feature.admin.model.AdminUserItemDto
import ir.speaking.feature.admin.model.AdminUserStatusUpdateRequest
import ir.speaking.feature.admin.user.service.AdminUserService
import java.util.UUID

@OptIn(ExperimentalKtorApi::class)
fun Route.adminUserRouting(
    adminUserService: AdminUserService,
    auditLogService: AuditLogService
) {
    route("/api/admin/users") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            get {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
                val search = call.request.queryParameters["search"]
                val status = call.request.queryParameters["status"]

                val (total, users) = adminUserService.getUsers(page, limit, search, status)
                val totalPages = if (total == 0L) 1 else ((total + limit - 1) / limit).toInt()

                call.successRespond(
                    data = users,
                    pagingMeta = PagingMeta(
                        totalPages = totalPages,
                        totalItems = total,
                        page = page
                    ),
                    message = "لیست کاربران با موفقیت دریافت شد"
                )
            }.describe {
                tag("Admin Users")
                summary = "List and Search Users"
                description = "Paginated user listing with Persian phone normalization and status filters"
                parameters {
                    query("page") { description = "Page number (1-indexed)"; required = false }
                    query("limit") { description = "Page size (1-100)"; required = false }
                    query("search") { description = "Search query by mobile or name"; required = false }
                    query("status") { description = "Filter by status: ACTIVE, SUSPENDED, ALL"; required = false }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User list"
                        schema = jsonSchema<SuccessResponse<List<AdminUserItemDto>>>()
                    }
                }
            }

            get("/{userId}") {
                val idStr = call.parameters["userId"] ?: return@get call.failureRespond(HttpStatusCode.BadRequest, "شناسه کاربر الزامی است")
                val userId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@get call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه کاربر نامعتبر است")
                }

                val detail = adminUserService.getUserDetail(userId)
                if (detail == null) {
                    call.failureRespond(HttpStatusCode.NotFound, "کاربر یافت نشد")
                    return@get
                }

                call.successRespond(detail, message = "جزئیات کاربر دریافت شد")
            }.describe {
                tag("Admin Users")
                summary = "Get User Details"
                description = "Get detailed profile, completed stages count, and active subscription of a user"
                parameters {
                    path("userId") { description = "User UUID" }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "User details"
                        schema = jsonSchema<SuccessResponse<AdminUserDetailDto>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "User not found"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            post("/{userId}/status") {
                val principal = call.principal<AdminPrincipal>()
                val idStr = call.parameters["userId"] ?: return@post call.failureRespond(HttpStatusCode.BadRequest, "شناسه کاربر الزامی است")
                val userId = try { UUID.fromString(idStr) } catch (_: Exception) {
                    return@post call.failureRespond(HttpStatusCode.BadRequest, "فرمت شناسه کاربر نامعتبر است")
                }

                val request = try {
                    call.receive<AdminUserStatusUpdateRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست وضعیت نامعتبر است")
                    return@post
                }

                val updated = adminUserService.updateUserStatus(userId, request.status, request.reason)
                if (!updated) {
                    call.failureRespond(HttpStatusCode.NotFound, "کاربر یافت نشد")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "USER_STATUS_CHANGE",
                    targetType = "USER",
                    targetId = userId.toString(),
                    detailsJson = """{"status":"${request.status}","reason":"${request.reason ?: ""}"}"""
                )

                call.successRespond(mapOf("success" to "true", "status" to request.status), message = "وضعیت کاربر با موفقیت تغییر یافت")
            }.describe {
                tag("Admin Users")
                summary = "Update User Status"
                description = "Activate or suspend a user account"
                parameters {
                    path("userId") { description = "User UUID" }
                }
                requestBody {
                    description = "Status update payload"
                    schema = jsonSchema<AdminUserStatusUpdateRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Status updated"
                        schema = jsonSchema<SuccessResponse<Map<String, String>>>()
                    }
                }
            }
        }
    }
}
