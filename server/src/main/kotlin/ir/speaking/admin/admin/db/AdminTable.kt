package ir.speaking.admin.admin.db

import ir.speaking.admin.admin.model.Admin
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.*

object AdminTable : UUIDTable("admin") {
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 60) // BCrypt hash length
}


class AdminDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<AdminDAO>(AdminTable)

    var username by AdminTable.username
    var passwordHash by AdminTable.passwordHash

}

fun AdminDAO.toModel() = Admin(
    uid = id.value,
    userName = username,
)

