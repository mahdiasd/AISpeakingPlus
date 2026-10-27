package ir.speaking.feature.announcement.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AnnouncementTable : UUIDTable("announcements") {
    val title = varchar("title", 255)
    val message = text("message")
    val type = varchar("type", 32).default("INFO") // INFO, WARNING, ERROR, SUCCESS
    val imageUrl = text("image_url").nullable()
    val isBlocking = bool("is_blocking").default(false)
    val isActive = bool("is_active").default(false).index()

    // Primary button (required)
    val primaryButtonText = varchar("primary_button_text", 64).default("فهمیدم")
    val primaryButtonAction = varchar("primary_button_action", 32).default("CLOSE") // CLOSE, OPEN_URL, NAVIGATE, RELOAD
    val primaryButtonValue = text("primary_button_value").nullable()

    // Secondary button (optional)
    val secondaryButtonText = varchar("secondary_button_text", 64).nullable()
    val secondaryButtonAction = varchar("secondary_button_action", 32).nullable()
    val secondaryButtonValue = text("secondary_button_value").nullable()

    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
