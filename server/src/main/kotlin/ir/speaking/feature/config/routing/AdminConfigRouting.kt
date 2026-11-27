package ir.speaking.feature.config.routing

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
import ir.speaking.feature.config.dto.AppVersionConfigDto
import ir.speaking.feature.config.dto.UpdateAppVersionConfigRequest
import ir.speaking.feature.config.service.AppConfigService
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@OptIn(ExperimentalKtorApi::class)
fun Route.adminConfigRouting(
    appConfigServiceProvider: () -> AppConfigService,
    auditLogService: AuditLogService
) {
    route("/api/admin/config/app-version") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {
            get {
                val config = appConfigServiceProvider().getAppVersionConfig()
                call.successRespond(config, message = "تنظیمات نسخه اپلیکیشن با موفقیت دریافت شد")
            }.describe {
                tag("Admin App Config")
                summary = "Get App Version Configuration"
                description = "Returns the current dynamic app version and update settings (Android)"
                responses {
                    HttpStatusCode.OK {
                        description = "Current app version configuration"
                        schema = jsonSchema<SuccessResponse<AppVersionConfigDto>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Admin authentication required"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }

            put {
                val principal = call.principal<AdminPrincipal>()
                val req = try {
                    call.receive<UpdateAppVersionConfigRequest>()
                } catch (e: Exception) {
                    call.failureRespond(HttpStatusCode.BadRequest, "فرمت درخواست نامعتبر است: ${e.message}")
                    return@put
                }

                val updated = try {
                    appConfigServiceProvider().updateAppVersionConfig(req)
                } catch (e: IllegalArgumentException) {
                    call.failureRespond(HttpStatusCode.BadRequest, e.message ?: "داده‌های ورودی نامعتبر است")
                    return@put
                }

                auditLogService.log(
                    adminId = principal?.id,
                    action = "APP_VERSION_CONFIG_UPDATE",
                    targetType = "APP_VERSION_CONFIG",
                    targetId = updated.platform,
                    detailsJson = buildJsonObject {
                        put("lastVersion", updated.lastVersion)
                        put("minVersion", updated.minVersion)
                        put("suggestVersion", updated.suggestVersion)
                        put("link", updated.link)
                        put("message", updated.message)
                    }.toString()
                )

                call.successRespond(updated, message = "تنظیمات نسخه اپلیکیشن با موفقیت به‌روزرسانی شد")
            }.describe {
                tag("Admin App Config")
                summary = "Update App Version Configuration"
                description = "Updates dynamic app version numbers, CafeBazaar download link, and Persian update message"
                requestBody {
                    description = "App version configuration update payload"
                    required = true
                    schema = jsonSchema<UpdateAppVersionConfigRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Updated app version configuration"
                        schema = jsonSchema<SuccessResponse<AppVersionConfigDto>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "Invalid payload or version constraints"
                        schema = jsonSchema<FailureResponse>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "Admin authentication required"
                        schema = jsonSchema<FailureResponse>()
                    }
                }
            }
        }
    }
}
