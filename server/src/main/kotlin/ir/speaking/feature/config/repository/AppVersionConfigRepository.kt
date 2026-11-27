package ir.speaking.feature.config.repository

import ir.speaking.core.utils.suspendTransaction
import ir.speaking.feature.config.db.AppVersionConfigTable
import ir.speaking.feature.config.dto.AppVersionConfigDto
import ir.speaking.feature.config.dto.UpdateAppVersionConfigRequest
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
open class AppVersionConfigRepository {

    open suspend fun findByPlatform(platform: String = "android"): AppVersionConfigDto? = suspendTransaction {
        val normalizedPlatform = platform.trim().lowercase()
        AppVersionConfigTable
            .selectAll()
            .where { AppVersionConfigTable.platform eq normalizedPlatform }
            .firstOrNull()
            ?.toDto()
    }

    open suspend fun upsert(
        platform: String = "android",
        req: UpdateAppVersionConfigRequest
    ): AppVersionConfigDto = suspendTransaction {
        val normalizedPlatform = platform.trim().lowercase()
        val now = Clock.System.now()
        val existing = AppVersionConfigTable
            .selectAll()
            .where { AppVersionConfigTable.platform eq normalizedPlatform }
            .firstOrNull()

        if (existing == null) {
            AppVersionConfigTable.insert {
                it[AppVersionConfigTable.platform] = normalizedPlatform
                it[lastVersion] = req.lastVersion
                it[minVersion] = req.minVersion
                it[suggestVersion] = req.suggestVersion
                it[link] = req.link
                it[message] = req.message
                it[updatedAt] = now
            }
        } else {
            AppVersionConfigTable.update({ AppVersionConfigTable.platform eq normalizedPlatform }) {
                it[lastVersion] = req.lastVersion
                it[minVersion] = req.minVersion
                it[suggestVersion] = req.suggestVersion
                it[link] = req.link
                it[message] = req.message
                it[updatedAt] = now
            }
        }

        AppVersionConfigTable
            .selectAll()
            .where { AppVersionConfigTable.platform eq normalizedPlatform }
            .first()
            .toDto()
    }

    private fun ResultRow.toDto(): AppVersionConfigDto {
        return AppVersionConfigDto(
            platform = this[AppVersionConfigTable.platform],
            lastVersion = this[AppVersionConfigTable.lastVersion],
            minVersion = this[AppVersionConfigTable.minVersion],
            suggestVersion = this[AppVersionConfigTable.suggestVersion],
            link = this[AppVersionConfigTable.link],
            message = this[AppVersionConfigTable.message],
            updatedAt = this[AppVersionConfigTable.updatedAt].toString()
        )
    }
}
