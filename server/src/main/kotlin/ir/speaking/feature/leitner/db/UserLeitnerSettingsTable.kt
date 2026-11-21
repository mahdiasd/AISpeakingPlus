package ir.speaking.feature.leitner.db

import ir.speaking.feature.user.db.UserTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserLeitnerSettingsTable : Table("user_leitner_settings") {
    val userId = reference("user_id", UserTable, onDelete = ReferenceOption.CASCADE).uniqueIndex()
    val reminderTime = varchar("reminder_time", 16).default("20:00")
    val reminderEnabled = bool("reminder_enabled").default(true)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(userId)
}
