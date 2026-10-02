package ir.speaking.feature.admin.dashboard.routing

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import ir.speaking.core.response.SuccessResponse
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.dashboard.service.AdminDashboardService
import ir.speaking.feature.admin.model.AdminAuditLogItemDto
import ir.speaking.feature.admin.model.AdminDashboardStatsDto

@OptIn(ExperimentalKtorApi::class)
fun Route.adminDashboardRouting(
    adminDashboardService: AdminDashboardService,
    auditLogService: AuditLogService
) {
    route("/api/admin") {
        authenticate(MyConstant.ADMIN_JWT_NAME) {

            get("/dashboard/stats") {
                val stats = adminDashboardService.getStats()
                call.successRespond(stats, message = "آمار کلی داشبورد دریافت شد")
            }.describe {
                tag("Admin Dashboard")
                summary = "Get Dashboard KPI Stats"
                description = "Get aggregate statistics on learners, subscriptions, and stages"
                responses {
                    HttpStatusCode.OK {
                        description = "Dashboard stats"
                        schema = jsonSchema<SuccessResponse<AdminDashboardStatsDto>>()
                    }
                }
            }

            get("/audit-logs") {
                val limit = (call.request.queryParameters["limit"]?.toIntOrNull() ?: 50).coerceIn(1, 200)
                val logs = auditLogService.getRecentLogs(limit)
                call.successRespond(logs, message = "لاگ‌های عملیات ادمین دریافت شد")
            }.describe {
                tag("Admin Dashboard")
                summary = "Get Audit Logs"
                description = "Get immutable administrative activity audit trail"
                parameters {
                    query("limit") {
                        description = "Maximum number of audit logs to retrieve"
                        required = false
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "Audit logs list"
                        schema = jsonSchema<SuccessResponse<List<AdminAuditLogItemDto>>>()
                    }
                }
            }
        }
    }
}
