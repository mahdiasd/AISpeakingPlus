package ir.speaking.feature.user_device_info.db

import ir.speaking.feature.user.db.UserDAO
import ir.speaking.feature.user.db.UserTable
import ir.speaking.feature.user_device_info.model.UserDeviceInfo
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

object UserDeviceInfoTable : UUIDTable("user_device_info") {
    val user = reference("user_id", UserTable).uniqueIndex()
    val firebaseToken = varchar("firebase_token", 255).nullable()
    val deviceName = varchar("device_name", 100).nullable()
    val androidVersion = varchar("android_version", 50).nullable()
    val lastLogin = timestamp("last_login")
}

class UserDeviceInfoDAO(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<UserDeviceInfoDAO>(UserDeviceInfoTable)

    var user by UserDAO referencedOn UserDeviceInfoTable.user
    var firebaseToken by UserDeviceInfoTable.firebaseToken
    var deviceName by UserDeviceInfoTable.deviceName
    var androidVersion by UserDeviceInfoTable.androidVersion
    var lastLogin by UserDeviceInfoTable.lastLogin
}

fun UserDeviceInfoDAO.toModel() = UserDeviceInfo(
    id = id.value,
    userId = user.id.value,
    firebaseToken = firebaseToken,
    deviceName = deviceName,
    androidVersion = androidVersion,
    lastLogin = lastLogin
)
