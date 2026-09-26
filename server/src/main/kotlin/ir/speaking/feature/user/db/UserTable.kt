package ir.speaking.feature.user.db

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object UserTable : UUIDTable("users") {
    val nickName = varchar("nick_name", 100).default("Learner")
    val firstName = varchar("first_name", 100).nullable()
    val lastName = varchar("last_name", 100).nullable()
    val mobile = varchar("phone_number", 20).uniqueIndex()
    val gender = varchar("gender", 16).nullable()
    val score = integer("score").default(0)
    val avatar = varchar("avatar", 255).default("default_avatar")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}
