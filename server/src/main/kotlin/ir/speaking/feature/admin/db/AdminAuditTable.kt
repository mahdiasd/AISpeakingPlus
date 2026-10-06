package ir.speaking.feature.admin.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AdminAuditTable : UUIDTable("admin_audit_logs") {
    val adminId = reference("admin_id", AdminUserTable, onDelete = ReferenceOption.SET_NULL).nullable().index()
    val action = varchar("action", 64).index()
    val targetType = varchar("target_type", 32)
    val targetId = varchar("target_id", 64)
    val detailsJson = text("details_json")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp).index()
}
