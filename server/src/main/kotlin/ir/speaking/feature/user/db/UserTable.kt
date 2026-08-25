package ir.speaking.feature.user.db

import ir.speaking.feature.user.model.Gender
import ir.speaking.feature.user.model.LanguageLevel
import ir.speaking.feature.user.model.User
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

object UserTable : UUIDTable("user") {
    val nickName = varchar("nick_name", 50)
    val firstName = varchar("first_name", 50).nullable()
    val lastName = varchar("last_name", 50).nullable()
    val mobile = varchar("phone_number", 11)
    val gender = varchar("gender", 5).nullable()
    val age = integer("age").nullable()
    val score = integer("score")
    val languageLevel = varchar("language_level", 2)
    val active = bool("active")
    val avatar = varchar("avatar", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}

class UserDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDAO>(UserTable)

    var nickName by UserTable.nickName
    var firstName by UserTable.firstName
    var lastName by UserTable.lastName
    var mobile by UserTable.mobile
    var gender by UserTable.gender
    var age by UserTable.age
    var score by UserTable.score
    var languageLevel by UserTable.languageLevel
    var active by UserTable.active
    var avatar by UserTable.avatar
    var createdAt by UserTable.createdAt
    var updatedAt by UserTable.updatedAt
}

fun UserDAO.toModel() =
    User(
        uid = id.value,
        nickName = nickName,
        firstName = firstName,
        lastName = lastName,
        mobile = mobile,
        gender = gender?.let { Gender.valueOf(it) },
        age = age,
        score = score,
        active = active,
        languageLevel = LanguageLevel.valueOf(languageLevel),
        avatar = avatar,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
