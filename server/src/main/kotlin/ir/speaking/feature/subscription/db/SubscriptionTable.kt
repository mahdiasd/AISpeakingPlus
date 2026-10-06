package ir.speaking.feature.subscription.db

import ir.speaking.feature.admin.db.AdminUserTable
import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object SubscriptionTable : UUIDTable("subscriptions") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE)
    val planType = varchar("plan_type", 32)
    val startedAt = timestamp("started_at").defaultExpression(CurrentTimestamp)
    val expiresAt = timestamp("expires_at").index()
    val status = varchar("status", 32).default("ACTIVE")
    val grantSource = varchar("grant_source", 32).default("PAYMENT_GATEWAY").index()
    val grantedBy = reference("granted_by", AdminUserTable, onDelete = ReferenceOption.SET_NULL).nullable()
    val grantReason = text("grant_reason").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
