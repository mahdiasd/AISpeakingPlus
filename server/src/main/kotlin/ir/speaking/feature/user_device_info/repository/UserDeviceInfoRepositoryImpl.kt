package ir.speaking.feature.user_device_info.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.user.db.UserDAO
import ir.speaking.feature.user_device_info.db.UserDeviceInfoDAO
import ir.speaking.feature.user_device_info.db.UserDeviceInfoTable
import ir.speaking.feature.user_device_info.db.toModel
import ir.speaking.feature.user_device_info.model.UserDeviceInfo
import org.koin.core.annotation.Single
import java.util.*

@Single
class UserDeviceInfoRepositoryImpl : UserDeviceInfoRepository {
    override suspend fun upsert(info: UserDeviceInfo): UserDeviceInfo = suspendTransaction {
        val existing = UserDeviceInfoDAO
            .find { UserDeviceInfoTable.user eq info.userId }
            .firstOrNull()

        if (existing != null) {
            existing.firebaseToken = info.firebaseToken
            existing.deviceName = info.deviceName
            existing.androidVersion = info.androidVersion
            existing.lastLogin = info.lastLogin
            existing.toModel()
        } else {
            UserDeviceInfoDAO.new {
                user = UserDAO[info.userId]
                firebaseToken = info.firebaseToken
                deviceName = info.deviceName
                androidVersion = info.androidVersion
                lastLogin = info.lastLogin
            }.toModel()
        }
    }

    override suspend fun getByUserId(userId: UUID): UserDeviceInfo? = suspendTransaction {
        UserDeviceInfoDAO
            .find { UserDeviceInfoTable.user eq userId }
            .firstOrNull()
            ?.toModel()
    }

    override suspend fun getAllFirebaseTokens(): List<String> = suspendTransaction {
        UserDeviceInfoTable
            .select(UserDeviceInfoTable.firebaseToken)
            .mapNotNull { it[UserDeviceInfoTable.firebaseToken] }
            .filter { it.isNotBlank() }
    }

}