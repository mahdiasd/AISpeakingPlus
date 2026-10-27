package ir.speaking.feature.announcement.routing

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
import ir.speaking.feature.announcement.model.AnnouncementDto
import ir.speaking.feature.announcement.model.AnnouncementListResponse
import ir.speaking.feature.announcement.model.AnnouncementUpsertRequest
import ir.speaking.feature.announcement.service.AnnouncementService
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalKtorApi::class)
fun Route.adminAnnouncementRouting(
    announcementService: AnnouncementService,
    auditLogService: AuditLogService
) {
    route("/api/admin/announcements") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            get {
                val list = announcementService.getAllAnnouncements()
                val activeId = list.firstOrNull { it.isActive }?.id
                val response = AnnouncementListResponse(
                    announcements = list,
                    activeAnnouncementId = activeId
                )
                call.successRespond(response, message = "لیست اطلاعیه‌ها دریافت شد")
            }.describe {
                tag("Admin Announcements")
                summary = "List all announcements"
                description = "Get list of all announcements with their active status and button configurations"
                responses {
                    HttpStatusCode.OK {
                        description = "Announcements list"
                        schema = jsonSchema<SuccessResponse<AnnouncementListResponse>>()
                    }
                }
            }

            post {
                val principal = call.principal<AdminPrincipal>()
                val req = try {
                    call.receive<AnnouncementUpsertRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست نامعتبر است: ${e.message}")
                    return@post
                }

                val created = try {
                    announcementService.createAnnouncement(req)
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "داده‌های ورودی نامعتبر است")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "ANNOUNCEMENT_CREATE",
                    targetType = "ANNOUNCEMENT",
                    targetId = created.id,
                    detailsJson = buildJsonObject {
                        put("title", created.title)
                        put("type", created.type)
                        put("isActive", created.isActive)
                        put("isBlocking", created.isBlocking)
                    }.toString()
                )

                call.successRespond(created, message = "اطلاعیه با موفقیت ایجاد شد")
            }.describe {
                tag("Admin Announcements")
                summary = "Create an announcement"
                description = "Create a new announcement with primary and optional secondary action buttons"
                requestBody {
                    description = "Announcement creation payload"
                    schema = jsonSchema<AnnouncementUpsertRequest>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "Created announcement"
                        schema = jsonSchema<SuccessResponse<AnnouncementDto>>()
                    }
                }
            }

            put("/{id}") {
                val principal = call.principal<AdminPrincipal>()
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.failureRespond(HttpStatusCode.BadRequest, "شناسه اطلاعیه الزامی است")
                    return@put
                }

                val req = try {
                    call.receive<AnnouncementUpsertRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست نامعتبر است: ${e.message}")
                    return@put
                }

                val updated = try {
                    announcementService.updateAnnouncement(id, req)
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "داده‌های ورودی نامعتبر است")
                    return@put
                }

                if (updated == null) {
                    call.failureRespond(HttpStatusCode.NotFound, "اطلاعیه مورد نظر یافت نشد")
                    return@put
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "ANNOUNCEMENT_UPDATE",
                    targetType = "ANNOUNCEMENT",
                    targetId = updated.id,
                    detailsJson = buildJsonObject {
                        put("title", updated.title)
                        put("type", updated.type)
                        put("isActive", updated.isActive)
                        put("isBlocking", updated.isBlocking)
                    }.toString()
                )

                call.successRespond(updated, message = "اطلاعیه با موفقیت ویرایش شد")
            }.describe {
                tag("Admin Announcements")
                summary = "Update an announcement"
                description = "Update an existing announcement"
                requestBody {
                    description = "Announcement update payload"
                    schema = jsonSchema<AnnouncementUpsertRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Updated announcement"
                        schema = jsonSchema<SuccessResponse<AnnouncementDto>>()
                    }
                }
            }

            post("/{id}/toggle-active") {
                val principal = call.principal<AdminPrincipal>()
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.failureRespond(HttpStatusCode.BadRequest, "شناسه اطلاعیه الزامی است")
                    return@post
                }

                val updated = announcementService.toggleActive(id)
                if (updated == null) {
                    call.failureRespond(HttpStatusCode.NotFound, "اطلاعیه مورد نظر یافت نشد")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = if (updated.isActive) "ANNOUNCEMENT_ACTIVATE" else "ANNOUNCEMENT_DEACTIVATE",
                    targetType = "ANNOUNCEMENT",
                    targetId = updated.id,
                    detailsJson = buildJsonObject {
                        put("isActive", updated.isActive)
                    }.toString()
                )

                val msg = if (updated.isActive) "اطلاعیه با موفقیت فعال شد" else "اطلاعیه با موفقیت غیرفعال شد"
                call.successRespond(updated, message = msg)
            }.describe {
                tag("Admin Announcements")
                summary = "Toggle announcement active status"
                description = "Activate or deactivate an announcement. Activating one automatically deactivates all others."
                responses {
                    HttpStatusCode.OK {
                        description = "Updated announcement status"
                        schema = jsonSchema<SuccessResponse<AnnouncementDto>>()
                    }
                }
            }

            delete("/{id}") {
                val principal = call.principal<AdminPrincipal>()
                val id = call.parameters["id"]
                if (id.isNullOrBlank()) {
                    call.failureRespond(HttpStatusCode.BadRequest, "شناسه اطلاعیه الزامی است")
                    return@delete
                }

                val deleted = announcementService.deleteAnnouncement(id)
                if (!deleted) {
                    call.failureRespond(HttpStatusCode.NotFound, "اطلاعیه مورد نظر یافت نشد یا حذف ناموفق بود")
                    return@delete
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "ANNOUNCEMENT_DELETE",
                    targetType = "ANNOUNCEMENT",
                    targetId = id,
                    detailsJson = "{}"
                )

                call.successRespond(mapOf("deleted" to true), message = "اطلاعیه با موفقیت حذف شد")
            }.describe {
                tag("Admin Announcements")
                summary = "Delete an announcement"
                description = "Delete an announcement permanently"
                responses {
                    HttpStatusCode.OK {
                        description = "Deletion confirmation"
                        schema = jsonSchema<SuccessResponse<Map<String, Boolean>>>()
                    }
                }
            }
        }
    }
}
