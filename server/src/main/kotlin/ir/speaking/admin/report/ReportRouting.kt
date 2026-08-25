package ir.speaking.admin.report

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import ir.speaking.core.response.successRespond
import ir.speaking.core.utils.MyConstant
import ir.speaking.core.utils.toUUID
import org.koin.ktor.ext.inject

fun Application.reportRouting() {
    val reportRepository by inject<ReportRepository>()

    routing {
        route("/api/v1/admin/reports") {
            authenticate(MyConstant.ADMIN_JWT_NAME) {
                getUsers(reportRepository)
                getUser(reportRepository)
                getPurchases(reportRepository)
                getPurchase(reportRepository)
            }
        }
    }
}

private fun Route.getUsers(reportRepository: ReportRepository) {
    get("/users") {
        val page = call.parameters["page"]?.toIntOrNull() ?: 1
        val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
        // دریافت پارامترهای جدید برای تاریخ و مرتب‌سازی
        val date = call.parameters["date"]
        val sortBy = call.parameters["sortBy"]
        val reports = reportRepository.getUsersReportPaginated(page, pageSize, date, sortBy)
        call.successRespond(reports)
    }
}

private fun Route.getUser(reportRepository: ReportRepository) {
    get("/users/{userId}") {
        val userId = call.parameters["userId"].toUUID()
        val report = reportRepository.getUserReport(userId)
        call.successRespond(report)
    }
}

private fun Route.getPurchases(reportRepository: ReportRepository) {
    get("/purchases") {
        val page = call.parameters["page"]?.toIntOrNull() ?: 1
        val pageSize = call.parameters["pageSize"]?.toIntOrNull() ?: 20
        val hasPaid = call.parameters["hasPaid"]?.toBoolean() ?: false
        val reports = reportRepository.getPurchasesReportPaginated(page, pageSize, hasPaid)
        call.successRespond(reports)
    }
}

private fun Route.getPurchase(reportRepository: ReportRepository) {
    get("/purchases/{purchaseId}") {
        val purchaseId = call.parameters["purchaseId"].toUUID()
        val report = reportRepository.getPurchaseReport(purchaseId)
        call.successRespond(report)
    }
}