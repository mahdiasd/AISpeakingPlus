package ir.speaking.admin.report

import java.util.*

interface ReportRepository {
    suspend fun getUserReport(userId: UUID): UserReportDto?

    suspend fun getUsersReportPaginated(page: Int, pageSize: Int, date: String?, sortBy: String?): List<UserReportDto>

    suspend fun getPurchaseReport(purchaseId: UUID): PurchaseReportDto?

    suspend fun getPurchasesReportPaginated(page: Int, pageSize: Int, hasPaid: Boolean): List<PurchaseReportDto>
}