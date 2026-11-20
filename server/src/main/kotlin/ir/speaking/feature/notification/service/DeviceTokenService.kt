package ir.speaking.feature.notification.service

import ir.speaking.core.network.utils.PrintHelper
import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.admin.notification.service.AdminNotificationService
import ir.speaking.feature.notification.db.DeviceTokensTable
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class DeviceTokenService(
    private val adminNotificationService: AdminNotificationService
) {
    suspend fun registerOrUpdateToken(
        token: String,
        userId: UUID? = null,
        platform: String = "android",
        topic: String = "AISpeakingPublic",
        deviceName: String? = null,
        appVersion: String? = null
    ): Boolean {
        if (token.isBlank()) return false
        val cleanToken = token.trim()
        val cleanTopic = topic.trim().ifBlank { "AISpeakingPublic" }

        return try {
            suspendTransaction {
                val existing = DeviceTokensTable.selectAll()
                    .where { DeviceTokensTable.token eq cleanToken }
                    .firstOrNull()

                if (existing == null) {
                    DeviceTokensTable.insert {
                        it[id] = UUID.randomUUID()
                        it[DeviceTokensTable.userId] = userId
                        it[DeviceTokensTable.token] = cleanToken
                        it[DeviceTokensTable.platform] = platform
                        it[DeviceTokensTable.topic] = cleanTopic
                        it[DeviceTokensTable.deviceName] = deviceName
                        it[DeviceTokensTable.appVersion] = appVersion
                        it[createdAt] = Clock.System.now()
                        it[updatedAt] = Clock.System.now()
                    }
                } else {
                    val tokenId = existing[DeviceTokensTable.id]
                    DeviceTokensTable.update({ DeviceTokensTable.id eq tokenId }) {
                        if (userId != null) {
                            it[DeviceTokensTable.userId] = userId
                        }
                        it[DeviceTokensTable.platform] = platform
                        it[DeviceTokensTable.topic] = cleanTopic
                        if (deviceName != null) {
                            it[DeviceTokensTable.deviceName] = deviceName
                        }
                        if (appVersion != null) {
                            it[DeviceTokensTable.appVersion] = appVersion
                        }
                        it[updatedAt] = Clock.System.now()
                    }
                }
            }

            // Also subscribe to FCM topic in Firebase
            try {
                adminNotificationService.subscribeTokenToTopic(cleanToken, cleanTopic)
            } catch (fe: Exception) {
                PrintHelper.warning("Failed to subscribe token to Firebase topic: ${fe.message}")
            }

            true
        } catch (e: Exception) {
            PrintHelper.error("Error saving device token: ${e.message}")
            false
        }
    }
}
