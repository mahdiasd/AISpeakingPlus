package ir.speaking.feature.admin.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*
import ir.speaking.feature.admin.audit.service.AuditLogService
import ir.speaking.feature.admin.auth.AdminAuthService
import ir.speaking.feature.admin.auth.routing.adminAuthRouting
import ir.speaking.feature.admin.dashboard.routing.adminDashboardRouting
import ir.speaking.feature.admin.dashboard.service.AdminDashboardService
import ir.speaking.feature.admin.media.routing.adminMediaRouting
import ir.speaking.feature.admin.stage.routing.adminStageRouting
import ir.speaking.feature.admin.stage.service.AdminStageService
import ir.speaking.feature.admin.subscription.routing.adminSubscriptionRouting
import ir.speaking.feature.admin.subscription.service.AdminSubscriptionService
import ir.speaking.feature.admin.user.routing.adminUserRouting
import ir.speaking.feature.admin.user.service.AdminUserService

fun Application.adminRouting() {
    val auditLogService = AuditLogService()
    val adminAuthService = AdminAuthService()
    val adminStageService = AdminStageService()
    val adminUserService = AdminUserService()
    val adminSubscriptionService = AdminSubscriptionService()
    val adminDashboardService = AdminDashboardService()

    routing {
        adminAuthRouting(adminAuthService, auditLogService)
        adminStageRouting(adminStageService, auditLogService)
        adminUserRouting(adminUserService, auditLogService)
        adminSubscriptionRouting(adminSubscriptionService, auditLogService)
        adminDashboardRouting(adminDashboardService, auditLogService)
        adminMediaRouting()
    }
}
