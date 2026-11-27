package ir.speaking.feature.config.db

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object AppVersionConfigTable : Table("app_version_configs") {
    val platform = varchar("platform", 32).default("android")
    val lastVersion = integer("last_version").default(10)
    val minVersion = integer("min_version").default(10)
    val suggestVersion = integer("suggest_version").default(10)
    val link = text("link").default("https://cafebazaar.ir/app/ir.aispeaking")
    val message = text("message").default(
        "نسخه جدید AI Speaking با مراحل داستانی و هوش مصنوعی پیشرفته منتشر شد. برای ادامه لطفاً برنامه را بروزرسانی نمایید."
    )
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(platform)
}
