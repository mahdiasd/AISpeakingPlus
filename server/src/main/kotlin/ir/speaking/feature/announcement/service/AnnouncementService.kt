package ir.speaking.feature.announcement.service

import ir.speaking.feature.announcement.db.AnnouncementTable
import ir.speaking.feature.announcement.model.AnnouncementDto
import ir.speaking.feature.announcement.model.AnnouncementUpsertRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

class AnnouncementService {

    private fun ResultRow.toAnnouncementDto(): AnnouncementDto {
        return AnnouncementDto(
            id = this[AnnouncementTable.id].value.toString(),
            title = this[AnnouncementTable.title],
            message = this[AnnouncementTable.message],
            type = this[AnnouncementTable.type],
            imageUrl = this[AnnouncementTable.imageUrl],
            isBlocking = this[AnnouncementTable.isBlocking],
            isActive = this[AnnouncementTable.isActive],
            primaryButtonText = this[AnnouncementTable.primaryButtonText],
            primaryButtonAction = this[AnnouncementTable.primaryButtonAction],
            primaryButtonValue = this[AnnouncementTable.primaryButtonValue],
            secondaryButtonText = this[AnnouncementTable.secondaryButtonText],
            secondaryButtonAction = this[AnnouncementTable.secondaryButtonAction],
            secondaryButtonValue = this[AnnouncementTable.secondaryButtonValue],
            createdAt = this[AnnouncementTable.createdAt].toString(),
            updatedAt = this[AnnouncementTable.updatedAt].toString()
        )
    }

    suspend fun getActiveAnnouncement(): AnnouncementDto? = newSuspendedTransaction(Dispatchers.IO) {
        AnnouncementTable
            .selectAll()
            .where { AnnouncementTable.isActive eq true }
            .orderBy(AnnouncementTable.updatedAt to SortOrder.DESC)
            .limit(1)
            .map { it.toAnnouncementDto() }
            .firstOrNull()
    }

    suspend fun getAllAnnouncements(): List<AnnouncementDto> = newSuspendedTransaction(Dispatchers.IO) {
        AnnouncementTable
            .selectAll()
            .orderBy(AnnouncementTable.createdAt to SortOrder.DESC)
            .map { it.toAnnouncementDto() }
    }

    suspend fun getAnnouncementById(id: String): AnnouncementDto? = newSuspendedTransaction(Dispatchers.IO) {
        val uuid = try {
            UUID.fromString(id)
        } catch (_: Exception) {
            return@newSuspendedTransaction null
        }

        AnnouncementTable
            .selectAll()
            .where { AnnouncementTable.id eq uuid }
            .map { it.toAnnouncementDto() }
            .firstOrNull()
    }

    suspend fun createAnnouncement(req: AnnouncementUpsertRequest): AnnouncementDto = newSuspendedTransaction(Dispatchers.IO) {
        require(req.title.isNotBlank()) { "عنوان اطلاعیه نمی‌تواند خالی باشد" }
        require(req.message.isNotBlank()) { "متن اطلاعیه نمی‌تواند خالی باشد" }

        val now = Clock.System.now()

        // If newly created announcement is active, deactivate all other announcements first
        if (req.isActive) {
            AnnouncementTable.update({ AnnouncementTable.isActive eq true }) {
                it[isActive] = false
                it[updatedAt] = now
            }
        }

        val insertedId = AnnouncementTable.insertAndGetId {
            it[title] = req.title.trim()
            it[message] = req.message.trim()
            it[type] = req.type.uppercase().trim().ifBlank { "INFO" }
            it[imageUrl] = req.imageUrl?.trim()?.ifBlank { null }
            it[isBlocking] = req.isBlocking
            it[isActive] = req.isActive
            it[primaryButtonText] = req.primaryButtonText.trim().ifBlank { "فهمیدم" }
            it[primaryButtonAction] = req.primaryButtonAction.uppercase().trim().ifBlank { "CLOSE" }
            it[primaryButtonValue] = req.primaryButtonValue?.trim()?.ifBlank { null }
            it[secondaryButtonText] = req.secondaryButtonText?.trim()?.ifBlank { null }
            it[secondaryButtonAction] = req.secondaryButtonAction?.uppercase()?.trim()?.ifBlank { null }
            it[secondaryButtonValue] = req.secondaryButtonValue?.trim()?.ifBlank { null }
            it[createdAt] = now
            it[updatedAt] = now
        }

        AnnouncementTable
            .selectAll()
            .where { AnnouncementTable.id eq insertedId }
            .map { it.toAnnouncementDto() }
            .first()
    }

    suspend fun updateAnnouncement(id: String, req: AnnouncementUpsertRequest): AnnouncementDto? = newSuspendedTransaction(Dispatchers.IO) {
        require(req.title.isNotBlank()) { "عنوان اطلاعیه نمی‌تواند خالی باشد" }
        require(req.message.isNotBlank()) { "متن اطلاعیه نمی‌تواند خالی باشد" }

        val uuid = try {
            UUID.fromString(id)
        } catch (_: Exception) {
            return@newSuspendedTransaction null
        }

        val exists = AnnouncementTable.selectAll().where { AnnouncementTable.id eq uuid }.count() > 0
        if (!exists) return@newSuspendedTransaction null

        val now = Clock.System.now()

        if (req.isActive) {
            // Deactivate all other announcements
            AnnouncementTable.update({ AnnouncementTable.id neq uuid }) {
                it[isActive] = false
                it[updatedAt] = now
            }
        }

        AnnouncementTable.update({ AnnouncementTable.id eq uuid }) {
            it[title] = req.title.trim()
            it[message] = req.message.trim()
            it[type] = req.type.uppercase().trim().ifBlank { "INFO" }
            it[imageUrl] = req.imageUrl?.trim()?.ifBlank { null }
            it[isBlocking] = req.isBlocking
            it[isActive] = req.isActive
            it[primaryButtonText] = req.primaryButtonText.trim().ifBlank { "فهمیدم" }
            it[primaryButtonAction] = req.primaryButtonAction.uppercase().trim().ifBlank { "CLOSE" }
            it[primaryButtonValue] = req.primaryButtonValue?.trim()?.ifBlank { null }
            it[secondaryButtonText] = req.secondaryButtonText?.trim()?.ifBlank { null }
            it[secondaryButtonAction] = req.secondaryButtonAction?.uppercase()?.trim()?.ifBlank { null }
            it[secondaryButtonValue] = req.secondaryButtonValue?.trim()?.ifBlank { null }
            it[updatedAt] = now
        }

        AnnouncementTable
            .selectAll()
            .where { AnnouncementTable.id eq uuid }
            .map { it.toAnnouncementDto() }
            .firstOrNull()
    }

    suspend fun toggleActive(id: String): AnnouncementDto? = newSuspendedTransaction(Dispatchers.IO) {
        val uuid = try {
            UUID.fromString(id)
        } catch (_: Exception) {
            return@newSuspendedTransaction null
        }

        val row = AnnouncementTable.selectAll().where { AnnouncementTable.id eq uuid }.firstOrNull() ?: return@newSuspendedTransaction null
        val currentActive = row[AnnouncementTable.isActive]
        val newActive = !currentActive
        val now = Clock.System.now()

        if (newActive) {
            // Deactivate all other announcements first
            AnnouncementTable.update({ AnnouncementTable.id neq uuid }) {
                it[isActive] = false
                it[updatedAt] = now
            }
        }

        AnnouncementTable.update({ AnnouncementTable.id eq uuid }) {
            it[isActive] = newActive
            it[updatedAt] = now
        }

        AnnouncementTable
            .selectAll()
            .where { AnnouncementTable.id eq uuid }
            .map { it.toAnnouncementDto() }
            .firstOrNull()
    }

    suspend fun deleteAnnouncement(id: String): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val uuid = try {
            UUID.fromString(id)
        } catch (_: Exception) {
            return@newSuspendedTransaction false
        }

        val count = AnnouncementTable.deleteWhere { AnnouncementTable.id eq uuid }
        count > 0
    }
}
