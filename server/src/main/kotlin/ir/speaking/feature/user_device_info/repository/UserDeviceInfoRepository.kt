package ir.speaking.feature.user_device_info.repository

import ir.speaking.feature.user_device_info.model.UserDeviceInfo
import java.util.*

interface UserDeviceInfoRepository {
    suspend fun upsert(info: UserDeviceInfo): UserDeviceInfo
    suspend fun getByUserId(userId: UUID): UserDeviceInfo?

    suspend fun getAllFirebaseTokens(): List<String> 

}