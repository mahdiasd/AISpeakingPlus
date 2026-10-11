package ir.speaking.feature.admin.stage.routing

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
import ir.speaking.feature.admin.model.AdminStageItemDto
import ir.speaking.feature.admin.model.AdminStageReorderRequest
import ir.speaking.feature.admin.model.AdminStageUpsertRequest
import ir.speaking.feature.admin.stage.service.AdminStageService
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalKtorApi::class)
fun Route.adminStageRouting(
    adminStageService: AdminStageService,
    auditLogService: AuditLogService
) {
    route("/api/admin/stages") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            get {
                val status = call.request.queryParameters["status"]
                val stages = adminStageService.getAllStages(status)
                call.successRespond(stages, message = "لیست مراحل با موفقیت دریافت شد")
            }.describe {
                tag("Admin Stages")
                summary = "List Stages"
                description = "Get all stages including drafts and published stages"
                parameters {
                    query("status") {
                        description = "Filter by status: ALL, DRAFT, PUBLISHED, ARCHIVED"
                        required = false
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Stages list"
                        schema = jsonSchema<SuccessResponse<List<AdminStageItemDto>>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Unauthorized"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            get("/{id}") {
                val id = call.parameters["id"] ?: return@get call.failureRespond(HttpStatusCode.BadRequest, "شناسه مرحله الزامی است")
                val stage = adminStageService.getStageById(id)
                if (stage == null) {
                    call.failureRespond(HttpStatusCode.NotFound, "مرحله با این شناسه یافت نشد")
                    return@get
                }
                call.successRespond(stage, message = "اطلاعات مرحله دریافت شد")
            }.describe {
                tag("Admin Stages")
                summary = "Get Stage Details"
                description = "Get detailed information of a specific stage"
                parameters {
                    path("id") {
                        description = "Unique stage identifier"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Stage details"
                        schema = jsonSchema<SuccessResponse<AdminStageItemDto>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "Stage not found"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            post {
                val principal = call.principal<AdminPrincipal>()
                val request = try {
                    call.receive<AdminStageUpsertRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست مرحله نامعتبر است: ${e.message}")
                    return@post
                }

                val saved = try {
                    adminStageService.upsertStage(request)
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.UnprocessableEntity, e.message ?: "اعتبارسنجی مقادیر با خطا مواجه شد")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "STAGE_UPSERT",
                    targetType = "STAGE",
                    targetId = saved.id,
                    detailsJson = buildJsonObject {
                        put("title", saved.title)
                        put("orderIndex", saved.orderIndex)
                        put("status", saved.status)
                    }.toString()
                )

                call.successRespond(saved, message = "مرحله با موفقیت ذخیره شد")
            }.describe {
                tag("Admin Stages")
                summary = "Create or Update Stage"
                description = "Create or update a stage from visual form or direct AI JSON payload"
                requestBody {
                    description = "Stage payload"
                    required = true
                    schema = jsonSchema<AdminStageUpsertRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Stage saved successfully"
                        schema = jsonSchema<SuccessResponse<AdminStageItemDto>>()
                    }
                    HttpStatusCode.UnprocessableEntity {
                        description = "Validation failed"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            post("/{id}/reorder") {
                val principal = call.principal<AdminPrincipal>()
                val id = call.parameters["id"] ?: return@post call.failureRespond(HttpStatusCode.BadRequest, "شناسه مرحله الزامی است")
                val request = try {
                    call.receive<AdminStageReorderRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست جابجایی مرحله نامعتبر است")
                    return@post
                }

                val reordered = try {
                    adminStageService.reorderStage(id, request.newOrderIndex, request.shiftSubsequent)
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.UnprocessableEntity, e.message ?: "شماره مرحله نامعتبر است")
                    return@post
                }

                if (reordered == null) {
                    call.failureRespond(HttpStatusCode.NotFound, "مرحله مورد نظر یافت نشد")
                    return@post
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "STAGE_REORDER",
                    targetType = "STAGE",
                    targetId = id,
                    detailsJson = buildJsonObject {
                        put("stageId", id)
                        put("newOrderIndex", reordered.orderIndex)
                        put("shiftSubsequent", request.shiftSubsequent)
                    }.toString()
                )

                call.successRespond(reordered, message = "ترتیب مرحله با موفقیت بروزرسانی شد")
            }.describe {
                tag("Admin Stages")
                summary = "Reorder Stage"
                description = "Safely swap or shift stage orderIndex without unique constraint conflicts"
                parameters {
                    path("id") {
                        description = "Unique stage identifier"
                    }
                }
                requestBody {
                    description = "Target order index"
                    required = true
                    schema = jsonSchema<AdminStageReorderRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Stage reordered successfully"
                        schema = jsonSchema<SuccessResponse<AdminStageItemDto>>()
                    }
                }
            }

            delete("/{id}") {
                val principal = call.principal<AdminPrincipal>()
                val id = call.parameters["id"] ?: return@delete call.failureRespond(HttpStatusCode.BadRequest, "شناسه مرحله الزامی است")

                val success = adminStageService.deleteStage(id)
                if (!success) {
                    call.failureRespond(HttpStatusCode.NotFound, "مرحله جهت حذف یافت نشد")
                    return@delete
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "STAGE_DELETE",
                    targetType = "STAGE",
                    targetId = id,
                    detailsJson = buildJsonObject {
                        put("stageId", id)
                    }.toString()
                )

                call.successRespond(mapOf("deleted" to true), message = "مرحله با موفقیت حذف شد")
            }.describe {
                tag("Admin Stages")
                summary = "Delete Stage"
                description = "Delete or archive a stage by ID"
                parameters {
                    path("id") {
                        description = "Unique stage identifier"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Stage deleted successfully"
                        schema = jsonSchema<SuccessResponse<Map<String, Boolean>>>()
                    }
                }
            }
        }
    }
}

