package ir.speaking.feature.notification.db

import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object DeviceTokensTable : Table("device_tokens") {
    val id = uuid("id")
    val userId = uuid("user_id").references(UserTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val token = varchar("token", 512).uniqueIndex("idx_device_token_unique")
    val platform = varchar("platform", 32).default("android")
    val topic = varchar("topic", 64).default("AISpeakingPublic")
    val deviceName = varchar("device_name", 128).nullable()
    val appVersion = varchar("app_version", 32).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(id)
}
