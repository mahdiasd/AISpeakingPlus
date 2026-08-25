package ir.speaking.feature.user_device_info.model

import kotlinx.datetime.Instant
import java.util.*

data class UserDeviceInfo(
    val id: UUID,
    val userId: UUID,
    val firebaseToken: String?,
    val deviceName: String?,
    val androidVersion: String?,
    val lastLogin: Instant
)
