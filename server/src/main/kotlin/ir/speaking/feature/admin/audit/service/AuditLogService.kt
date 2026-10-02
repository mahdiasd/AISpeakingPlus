package ir.speaking.feature.admin.audit.service

import ir.speaking.feature.admin.db.AdminAuditTable
import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.admin.model.AdminAuditLogItemDto
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AuditLogService {

    suspend fun log(
        adminId: UUID?,
        action: String,
        targetType: String,
        targetId: String,
        detailsJson: String
    ) = newSuspendedTransaction(Dispatchers.IO) {
        AdminAuditTable.insert {
            it[AdminAuditTable.adminId] = adminId
            it[AdminAuditTable.action] = action
            it[AdminAuditTable.targetType] = targetType
            it[AdminAuditTable.targetId] = targetId
            it[AdminAuditTable.detailsJson] = detailsJson
        }
    }

    suspend fun getRecentLogs(limit: Int = 50): List<AdminAuditLogItemDto> = newSuspendedTransaction(Dispatchers.IO) {
        val query = (AdminAuditTable leftJoin AdminUserTable)
            .selectAll()
            .orderBy(AdminAuditTable.createdAt, SortOrder.DESC)
            .limit(limit)

        query.map { row ->
            AdminAuditLogItemDto(
                id = row[AdminAuditTable.id].value.toString(),
                adminId = row[AdminAuditTable.adminId]?.value?.toString(),
                adminName = row.getOrNull(AdminUserTable.fullName) ?: "System / Anonymous",
                action = row[AdminAuditTable.action],
                targetType = row[AdminAuditTable.targetType],
                targetId = row[AdminAuditTable.targetId],
                detailsJson = row[AdminAuditTable.detailsJson],
                createdAt = row[AdminAuditTable.createdAt].toString()
            )
        }
    }
}
