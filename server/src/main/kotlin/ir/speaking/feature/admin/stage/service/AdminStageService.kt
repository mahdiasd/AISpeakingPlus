package ir.speaking.feature.admin.stage.service

import ir.speaking.feature.admin.model.AdminStageItemDto
import ir.speaking.feature.admin.model.AdminStageUpsertRequest
import ir.speaking.feature.stage.db.StageTable
import ir.speaking.feature.stage_progress.db.StageProgressTable
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class AdminStageService {

    suspend fun getAllStages(status: String? = null): List<AdminStageItemDto> = newSuspendedTransaction(Dispatchers.IO) {
        val query = StageTable.selectAll()
        if (!status.isNullOrBlank() && status != "ALL") {
            query.andWhere { StageTable.status eq status.uppercase() }
        }
        query.orderBy(StageTable.orderIndex, SortOrder.ASC).map { row ->
            toDto(row)
        }
    }

    suspend fun getStageById(id: String): AdminStageItemDto? = newSuspendedTransaction(Dispatchers.IO) {
        StageTable.selectAll().where { StageTable.id eq id }.singleOrNull()?.let { toDto(it) }
    }

    suspend fun upsertStage(request: AdminStageUpsertRequest): AdminStageItemDto = newSuspendedTransaction(Dispatchers.IO) {
        if (request.id.isBlank()) throw IllegalArgumentException("شناسه مرحله نمی‌تواند خالی باشد")
        if (request.orderIndex <= 0) throw IllegalArgumentException("شماره مرحله باید بزرگتر از صفر باشد")
        if (request.title.isBlank() || request.titleFa.isBlank()) throw IllegalArgumentException("عنوان انگلیسی و فارسی مرحله الزامی است")
        if (request.briefing.isBlank() || request.briefingFa.isBlank()) throw IllegalArgumentException("توضیحات و بریفینگ انگلیسی و فارسی مرحله الزامی است")

        val existing = StageTable.selectAll().where { StageTable.id eq request.id }.singleOrNull()
        val oldOrderIndex = existing?.get(StageTable.orderIndex)
        if (existing != null && oldOrderIndex != request.orderIndex) {
            // Temporarily move current stage out of the way to prevent unique index collisions
            StageTable.update(where = { StageTable.id eq request.id }) {
                it[orderIndex] = -999999
            }
        }

        val occupiedTarget = StageTable.selectAll()
            .where { (StageTable.orderIndex eq request.orderIndex) and (StageTable.id neq request.id) }
            .singleOrNull()

        if (request.shiftSubsequent || (existing == null && occupiedTarget != null)) {
            val conflicting = StageTable.selectAll()
                .where { (StageTable.orderIndex greaterEq request.orderIndex) and (StageTable.id neq request.id) }
                .orderBy(StageTable.orderIndex, SortOrder.DESC)
                .toList()
            for (row in conflicting) {
                val rowId = row[StageTable.id].value
                val currentIdx = row[StageTable.orderIndex]
                StageTable.update(where = { StageTable.id eq rowId }) {
                    it[orderIndex] = currentIdx + 1
                }
            }
        } else if (occupiedTarget != null && oldOrderIndex != null) {
            val otherId = occupiedTarget[StageTable.id].value
            StageTable.update(where = { StageTable.id eq otherId }) {
                it[orderIndex] = oldOrderIndex
            }
        }

        if (existing == null) {
            StageTable.insert {
                it[id] = request.id
                it[orderIndex] = request.orderIndex
                it[title] = request.title
                it[titleFa] = request.titleFa
                it[briefing] = request.briefing
                it[briefingFa] = request.briefingFa
                it[targetObjective] = request.targetObjective
                it[targetObjectiveFa] = request.targetObjectiveFa
                it[characterBehavior] = request.characterBehavior
                it[backgroundUrl] = request.backgroundUrl
                it[characterName] = request.characterName
                it[characterAvatarUrl] = request.characterAvatarUrl
                it[characterGender] = request.characterGender
                it[voiceId] = request.voiceId
                it[initialSpeaker] = request.initialSpeaker
                it[maxTurns] = request.maxTurns
                it[status] = request.status.uppercase()
            }
        } else {
            StageTable.update(where = { StageTable.id eq request.id }) {
                it[orderIndex] = request.orderIndex
                it[title] = request.title
                it[titleFa] = request.titleFa
                it[briefing] = request.briefing
                it[briefingFa] = request.briefingFa
                it[targetObjective] = request.targetObjective
                it[targetObjectiveFa] = request.targetObjectiveFa
                it[characterBehavior] = request.characterBehavior
                it[backgroundUrl] = request.backgroundUrl
                it[characterName] = request.characterName
                it[characterAvatarUrl] = request.characterAvatarUrl
                it[characterGender] = request.characterGender
                it[voiceId] = request.voiceId
                it[initialSpeaker] = request.initialSpeaker
                it[maxTurns] = request.maxTurns
                it[status] = request.status.uppercase()
            }
        }

        StageTable.selectAll().where { StageTable.id eq request.id }.single().let { toDto(it) }
    }

    suspend fun reorderStage(id: String, newOrderIndex: Int, shiftSubsequent: Boolean = true): AdminStageItemDto? = newSuspendedTransaction(Dispatchers.IO) {
        if (newOrderIndex <= 0) throw IllegalArgumentException("شماره مرحله باید بزرگتر از صفر باشد")
        val target = StageTable.selectAll().where { StageTable.id eq id }.singleOrNull() ?: return@newSuspendedTransaction null
        val oldOrderIndex = target[StageTable.orderIndex]
        if (oldOrderIndex == newOrderIndex) {
            return@newSuspendedTransaction toDto(target)
        }

        // Park target stage at a temporary negative index to avoid unique constraint collisions
        StageTable.update(where = { StageTable.id eq id }) {
            it[orderIndex] = -999999
        }

        val occupiedTarget = StageTable.selectAll()
            .where { (StageTable.orderIndex eq newOrderIndex) and (StageTable.id neq id) }
            .singleOrNull()

        if (occupiedTarget != null) {
            if (!shiftSubsequent) {
                // Direct swap with the stage currently at newOrderIndex
                val otherId = occupiedTarget[StageTable.id].value
                StageTable.update(where = { StageTable.id eq otherId }) {
                    it[orderIndex] = oldOrderIndex
                }
            } else if (newOrderIndex < oldOrderIndex) {
                // Moving earlier in sequence: shift [newOrderIndex, oldOrderIndex) up by +1 in DESC order
                val rowsToShift = StageTable.selectAll()
                    .where {
                        (StageTable.orderIndex greaterEq newOrderIndex) and
                            (StageTable.orderIndex less oldOrderIndex) and
                            (StageTable.id neq id)
                    }
                    .orderBy(StageTable.orderIndex, SortOrder.DESC)
                    .toList()
                for (row in rowsToShift) {
                    val rowId = row[StageTable.id].value
                    val currentIdx = row[StageTable.orderIndex]
                    StageTable.update(where = { StageTable.id eq rowId }) {
                        it[orderIndex] = currentIdx + 1
                    }
                }
            } else {
                // Moving later in sequence: shift (oldOrderIndex, newOrderIndex] down by -1 in ASC order
                val rowsToShift = StageTable.selectAll()
                    .where {
                        (StageTable.orderIndex greater oldOrderIndex) and
                            (StageTable.orderIndex lessEq newOrderIndex) and
                            (StageTable.id neq id)
                    }
                    .orderBy(StageTable.orderIndex, SortOrder.ASC)
                    .toList()
                for (row in rowsToShift) {
                    val rowId = row[StageTable.id].value
                    val currentIdx = row[StageTable.orderIndex]
                    StageTable.update(where = { StageTable.id eq rowId }) {
                        it[orderIndex] = currentIdx - 1
                    }
                }
            }
        }

        StageTable.update(where = { StageTable.id eq id }) {
            it[orderIndex] = newOrderIndex
        }

        StageTable.selectAll().where { StageTable.id eq id }.singleOrNull()?.let { toDto(it) }
    }

    suspend fun deleteStage(id: String): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val existing = StageTable.selectAll().where { StageTable.id eq id }.singleOrNull() ?: return@newSuspendedTransaction false
        val hasProgress = StageProgressTable.selectAll().where { StageProgressTable.stageId eq id }.count() > 0L
        if (hasProgress) {
            // Soft-archive if learners have progress on this stage (since FK is RESTRICT)
            StageTable.update(where = { StageTable.id eq id }) {
                it[status] = "ARCHIVED"
            } > 0
        } else {
            StageTable.deleteWhere { StageTable.id eq id } > 0
        }
    }

    private fun toDto(row: ResultRow): AdminStageItemDto {
        return AdminStageItemDto(
            id = row[StageTable.id].value,
            orderIndex = row[StageTable.orderIndex],
            title = row[StageTable.title],
            titleFa = row[StageTable.titleFa],
            briefing = row[StageTable.briefing],
            briefingFa = row[StageTable.briefingFa],
            targetObjective = row[StageTable.targetObjective],
            targetObjectiveFa = row[StageTable.targetObjectiveFa],
            characterBehavior = row[StageTable.characterBehavior],
            backgroundUrl = row[StageTable.backgroundUrl],
            characterName = row[StageTable.characterName],
            characterAvatarUrl = row[StageTable.characterAvatarUrl],
            characterGender = row[StageTable.characterGender],
            voiceId = row[StageTable.voiceId],
            initialSpeaker = row[StageTable.initialSpeaker],
            maxTurns = row[StageTable.maxTurns],
            status = row[StageTable.status],
            createdAt = row[StageTable.createdAt].toString()
        )
    }
}
