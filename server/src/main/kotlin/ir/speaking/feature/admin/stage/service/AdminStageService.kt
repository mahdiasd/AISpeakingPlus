package ir.speaking.feature.admin.stage.service

import ir.speaking.feature.admin.model.AdminStageItemDto
import ir.speaking.feature.admin.model.AdminStageUpsertRequest
import ir.speaking.feature.stage.db.StageTable
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

        if (request.shiftSubsequent) {
            StageTable.update(
                where = { (StageTable.orderIndex greaterEq request.orderIndex) and (StageTable.id neq request.id) }
            ) {
                with(SqlExpressionBuilder) {
                    it.update(orderIndex, orderIndex + 1)
                }
            }
        }

        val existing = StageTable.selectAll().where { StageTable.id eq request.id }.singleOrNull()
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

    suspend fun deleteStage(id: String): Boolean = newSuspendedTransaction(Dispatchers.IO) {
        val deleted = StageTable.deleteWhere { StageTable.id eq id }
        deleted > 0
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
